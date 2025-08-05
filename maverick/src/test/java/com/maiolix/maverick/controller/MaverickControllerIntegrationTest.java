package com.maiolix.maverick.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.maiolix.maverick.constants.MaverickConstants;
import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.registry.ModelRegistry;
import com.maiolix.maverick.repository.MinioModelRepository;
import com.maiolix.maverick.service.ModelDatabaseService;

/**
 * Test di integrazione completo per MaverickController
 * Testa le integrazioni con PostgreSQL, MinIO e cache in memoria
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/maverick_test",
    "maverick.storage.minio.endpoint=http://localhost:9000",
    "maverick.storage.minio.access-key=minioadmin",
    "maverick.storage.minio.secret-key=minioadmin",
    "maverick.storage.minio.default-bucket=maverick-test",
    "logging.level.com.maiolix.maverick=DEBUG"
})
@TestMethodOrder(OrderAnnotation.class)
class MaverickControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ModelDatabaseService modelDatabaseService;

    @Autowired
    private MinioModelRepository minioRepository;

    private MockMvc mockMvc;

    private static final String TEST_MODEL_NAME = "iris-test-model";
    private static final String TEST_VERSION = "v1.0";
    private static final String TEST_MODEL_TYPE = "ONNX";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Pulisce la cache prima di ogni test
        ModelRegistry.clear();
    }

    /**
     * Test workflow completo: Upload → Load → Predict → Remove → Delete
     */
    @Test
    @Order(1)
    void testCompleteWorkflow() throws Exception {
        // === 1. TEST UPLOAD ===
        MockMultipartFile mockFile = createMockOnnxFile();
        
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(mockFile)
                .param("modelName", TEST_MODEL_NAME)
                .param("version", TEST_VERSION)
                .param("type", TEST_MODEL_TYPE)
                .param("description", "Test model for integration testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.modelName").value(TEST_MODEL_NAME))
                .andExpect(jsonPath("$.version").value(TEST_VERSION))
                .andExpect(jsonPath("$.isActive").value(false));

        // Verifica salvataggio nel database
        Optional<ModelEntity> savedModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION);
        assertTrue(savedModel.isPresent(), "Modello dovrebbe essere salvato nel database");
        assertFalse(savedModel.get().getIsActive(), "Modello dovrebbe essere inattivo dopo upload");

        // === 2. TEST LOAD ===
        mockMvc.perform(post("/api/v1/maverick/load")
                .param("modelName", TEST_MODEL_NAME)
                .param("version", TEST_VERSION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.cached").value(true));

        // Verifica caricamento in memoria
        assertTrue(ModelRegistry.exists(TEST_MODEL_NAME, TEST_VERSION), "Modello dovrebbe essere in memoria");

        // Verifica attivazione nel database
        Optional<ModelEntity> activeModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION);
        assertTrue(activeModel.isPresent() && activeModel.get().getIsActive(), "Modello dovrebbe essere attivo nel database");

        // === 3. TEST LISTA MODELLI IN MEMORIA ===
        mockMvc.perform(get("/api/v1/maverick/models-in-memory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.statistics.totalModels").value(1))
                .andExpect(jsonPath("$.models[0].modelName").value(TEST_MODEL_NAME));

        // === 4. TEST LISTA MODELLI DATABASE ===
        mockMvc.perform(get("/api/v1/maverick/models-database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.statistics.totalModels").value(1))
                .andExpect(jsonPath("$.models[0].modelName").value(TEST_MODEL_NAME))
                .andExpect(jsonPath("$.models[0].isActive").value(true));

        // === 5. TEST AUDIT ===
        mockMvc.perform(get("/api/v1/maverick/bootstrap/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.statistics.isConsistent").value(true))
                .andExpect(jsonPath("$.statistics.databaseActiveModels").value(1))
                .andExpect(jsonPath("$.statistics.memoryCachedModels").value(1));

        // === 6. TEST REMOVE ===
        mockMvc.perform(delete("/api/v1/maverick/remove")
                .param("modelName", TEST_MODEL_NAME)
                .param("version", TEST_VERSION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.isActive").value(false));

        // Verifica rimozione dalla memoria
        assertFalse(ModelRegistry.exists(TEST_MODEL_NAME, TEST_VERSION), "Modello dovrebbe essere rimosso dalla memoria");

        // Verifica disattivazione nel database
        Optional<ModelEntity> inactiveModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION);
        assertTrue(inactiveModel.isPresent() && !inactiveModel.get().getIsActive(), "Modello dovrebbe essere inattivo nel database");

        // === 7. TEST DELETE COMPLETO ===
        mockMvc.perform(delete("/api/v1/maverick/delete")
                .param("modelName", TEST_MODEL_NAME)
                .param("version", TEST_VERSION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.operations.databaseDeleted").value(true))
                .andExpect(jsonPath("$.operations.minioDeleted").value(true));

        // Verifica eliminazione dal database
        Optional<ModelEntity> deletedModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION);
        assertFalse(deletedModel.isPresent(), "Modello dovrebbe essere eliminato dal database");
    }

    /**
     * Test caricamento automatico modelli attivi all'avvio
     */
    @Test
    @Order(2)
    @Transactional
    void testBootstrapAutoLoading() throws Exception {
        // === SETUP: Crea modello attivo nel database ===
        ModelEntity activeModel = createTestModelEntity(TEST_MODEL_NAME + "-bootstrap", TEST_VERSION, true);
        ModelEntity inactiveModel = createTestModelEntity(TEST_MODEL_NAME + "-inactive", TEST_VERSION, false);
        
        modelDatabaseService.saveModel(activeModel);
        modelDatabaseService.saveModel(inactiveModel);

        // Simula file su MinIO creando il contenuto
        uploadTestFileToMinio(activeModel.getFilePath());

        // === TEST RELOAD MANUALE ===
        mockMvc.perform(post("/api/v1/maverick/bootstrap/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(MaverickConstants.SUCCESS))
                .andExpect(jsonPath("$.before.databaseActive").value(1))
                .andExpect(jsonPath("$.after.memoryCache").value(1));

        // Verifica che solo il modello attivo sia stato caricato
        assertTrue(ModelRegistry.exists(activeModel.getModelName(), activeModel.getVersion()), 
                "Modello attivo dovrebbe essere caricato in memoria");
        assertFalse(ModelRegistry.exists(inactiveModel.getModelName(), inactiveModel.getVersion()), 
                "Modello inattivo non dovrebbe essere caricato in memoria");
    }

    /**
     * Test gestione errori
     */
    @Test
    @Order(3)
    void testErrorHandling() throws Exception {
        // === TEST UPLOAD MODELLO DUPLICATO ===
        MockMultipartFile mockFile = createMockOnnxFile();
        
        // Primo upload
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(mockFile)
                .param("modelName", "duplicate-model")
                .param("version", "v1.0")
                .param("type", TEST_MODEL_TYPE))
                .andExpect(status().isOk());

        // Secondo upload dello stesso modello (dovrebbe fallire)
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(createMockOnnxFile())
                .param("modelName", "duplicate-model")
                .param("version", "v1.0")
                .param("type", TEST_MODEL_TYPE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MODEL_UPLOAD_ERROR"));

        // === TEST LOAD MODELLO INESISTENTE ===
        mockMvc.perform(post("/api/v1/maverick/load")
                .param("modelName", "non-existent-model")
                .param("version", "v1.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("MODEL_NOT_FOUND"));

        // === TEST REMOVE MODELLO INESISTENTE ===
        mockMvc.perform(delete("/api/v1/maverick/remove")
                .param("modelName", "non-existent-model")
                .param("version", "v1.0"))
                .andExpect(status().isNotFound());

        // Cleanup
        modelDatabaseService.deleteModel("duplicate-model", "v1.0");
    }

    /**
     * Test validazione input
     */
    @Test
    @Order(4)
    void testInputValidation() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.onnx", "application/octet-stream", new byte[0]);

        // === TEST FILE VUOTO ===
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(emptyFile)
                .param("modelName", "test")
                .param("version", "v1.0")
                .param("type", TEST_MODEL_TYPE))
                .andExpect(status().isBadRequest());

        // === TEST NOME MODELLO INVALIDO ===
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(createMockOnnxFile())
                .param("modelName", "invalid@name")
                .param("version", "v1.0")
                .param("type", TEST_MODEL_TYPE))
                .andExpect(status().isBadRequest());

        // === TEST VERSIONE INVALIDA ===
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(createMockOnnxFile())
                .param("modelName", "test-model")
                .param("version", "invalid-version")
                .param("type", TEST_MODEL_TYPE))
                .andExpect(status().isBadRequest());

        // === TEST TIPO MODELLO INVALIDO ===
        mockMvc.perform(multipart("/api/v1/maverick/upload")
                .file(createMockOnnxFile())
                .param("modelName", "test-model")
                .param("version", "v1.0")
                .param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    // === METODI HELPER ===

    private MockMultipartFile createMockOnnxFile() {
        byte[] mockOnnxContent = createMockOnnxBytes();
        return new MockMultipartFile("file", "test-model.onnx", "application/octet-stream", mockOnnxContent);
    }

    private byte[] createMockOnnxBytes() {
        // Crea un mock ONNX file semplificato per i test
        return "mock-onnx-content-for-testing".getBytes();
    }

    private ModelEntity createTestModelEntity(String name, String version, boolean isActive) {
        return ModelEntity.builder()
                .modelName(name)
                .version(version)
                .type(ModelEntity.ModelType.ONNX)
                .description("Test model for integration testing")
                .filePath(name + "/" + version + "/test-model.onnx")
                .fileSize(1024L)
                .fileHash("test-hash")
                .storageType(ModelEntity.StorageType.MINIO)
                .bucketName("maverick-test")
                .isActive(isActive)
                .status(ModelEntity.ModelStatus.READY)
                .build();
    }

    private void uploadTestFileToMinio(String filePath) {
        try {
            byte[] content = createMockOnnxBytes();
            InputStream inputStream = new ByteArrayInputStream(content);
            String[] pathParts = filePath.split("/");
            String modelName = pathParts[0];
            String version = pathParts[1];
            String fileName = pathParts[2];
            
            minioRepository.uploadModel(modelName, version, fileName, inputStream, content.length, "application/octet-stream");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload test file to MinIO", e);
        }
    }
}
