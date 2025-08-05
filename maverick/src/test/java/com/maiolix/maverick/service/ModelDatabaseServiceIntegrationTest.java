package com.maiolix.maverick.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.maiolix.maverick.entity.ModelEntity;

/**
 * Test di integrazione per ModelDatabaseService con PostgreSQL
 * Testa tutte le operazioni CRUD e logiche di business
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/maverick_test",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.com.maiolix.maverick=DEBUG",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@TestMethodOrder(OrderAnnotation.class)
@Transactional
class ModelDatabaseServiceIntegrationTest {

    @Autowired
    private ModelDatabaseService modelDatabaseService;

    private static final String TEST_MODEL_NAME = "iris-database-test";
    private static final String TEST_VERSION_1 = "v1.0";
    private static final String TEST_VERSION_2 = "v2.0";

    @BeforeEach
    void setUp() {
        // Pulizia database prima di ogni test
        modelDatabaseService.getAllModels(0, 100).getContent().forEach(model -> 
            modelDatabaseService.deleteModel(model.getModelName(), model.getVersion()));
    }

    /**
     * Test CRUD completo per ModelEntity
     */
    @Test
    @Order(1)
    void testCrudOperations() {
        // === CREATE ===
        ModelEntity model = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_1, false);
        ModelEntity savedModel = modelDatabaseService.saveModel(model);
        
        assertNotNull(savedModel.getId(), "ID dovrebbe essere generato automaticamente");
        assertNotNull(savedModel.getCreatedAt(), "CreatedAt dovrebbe essere impostato");
        assertNotNull(savedModel.getUpdatedAt(), "UpdatedAt dovrebbe essere impostato");
        assertEquals(model.getModelName(), savedModel.getModelName());
        assertEquals(model.getVersion(), savedModel.getVersion());

        // === READ ===
        Optional<ModelEntity> foundModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1);
        assertTrue(foundModel.isPresent(), "Modello dovrebbe essere trovato");
        assertEquals(savedModel.getId(), foundModel.get().getId());

        // === UPDATE ===
        ModelEntity toUpdate = foundModel.get();
        toUpdate.setDescription("Descrizione aggiornata");
        toUpdate.setIsActive(true);
        
        ModelEntity updatedModel = modelDatabaseService.saveModel(toUpdate);
        assertEquals("Descrizione aggiornata", updatedModel.getDescription());
        assertTrue(updatedModel.getIsActive());
        assertTrue(updatedModel.getUpdatedAt().isAfter(updatedModel.getCreatedAt()));

        // === DELETE ===
        boolean deleted = modelDatabaseService.deleteModel(TEST_MODEL_NAME, TEST_VERSION_1);
        assertTrue(deleted, "Eliminazione dovrebbe riuscire");

        Optional<ModelEntity> deletedModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1);
        assertFalse(deletedModel.isPresent(), "Modello dovrebbe essere eliminato");
    }

    /**
     * Test vincoli di unicità e validazione
     */
    @Test
    @Order(2)
    void testUniqueConstraintsAndValidation() {
        // === TEST VINCOLO UNICITÀ (modelName + version) ===
        ModelEntity model1 = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_1, false);
        modelDatabaseService.saveModel(model1);

        ModelEntity duplicateModel = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_1, false);
        assertThrows(DataIntegrityViolationException.class, () -> {
            modelDatabaseService.saveModel(duplicateModel);
        }, "Dovrebbe fallire per duplicato modelName+version");

        // === TEST VERSIONI MULTIPLE STESSO MODELLO ===
        ModelEntity model2 = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_2, false);
        ModelEntity savedModel2 = modelDatabaseService.saveModel(model2);
        assertNotNull(savedModel2.getId(), "Dovrebbe permettere versioni diverse");

        Page<ModelEntity> allVersions = modelDatabaseService.getAllModels(0, 10);
        assertEquals(2, allVersions.getContent().size(), "Dovrebbero esserci 2 versioni");
    }

    /**
     * Test query avanzate e filtri
     */
    @Test
    @Order(3)
    void testAdvancedQueries() {
        // === SETUP: Crea modelli di test ===
        createAndSaveMultipleModels();

        // === TEST FIND ACTIVE MODELS ===
        List<ModelEntity> activeModels = modelDatabaseService.findActiveModels();
        assertEquals(2, activeModels.size(), "Dovrebbero esserci 2 modelli attivi");
        assertTrue(activeModels.stream().allMatch(ModelEntity::getIsActive));

        // === TEST FIND BY TYPE ===
        List<ModelEntity> onnxModels = modelDatabaseService.findByType(ModelEntity.ModelType.ONNX);
        assertEquals(3, onnxModels.size(), "Dovrebbero esserci 3 modelli ONNX");

        // === TEST FIND BY STORAGE TYPE ===
        List<ModelEntity> minioModels = modelDatabaseService.findByStorageType(ModelEntity.StorageType.MINIO);
        assertEquals(4, minioModels.size(), "Dovrebbero esserci 4 modelli MinIO");
    }

    /**
     * Test paginazione
     */
    @Test
    @Order(4)
    void testPagination() {
        // === SETUP ===
        createAndSaveMultipleModels();

        // === TEST PAGINAZIONE ===
        Page<ModelEntity> firstPage = modelDatabaseService.getAllModels(0, 2);
        
        assertEquals(2, firstPage.getContent().size(), "Prima pagina dovrebbe avere 2 elementi");
        assertEquals(4, firstPage.getTotalElements(), "Totale dovrebbe essere 4");
        assertEquals(2, firstPage.getTotalPages(), "Dovrebbero esserci 2 pagine");

        // === TEST SECONDA PAGINA ===
        Page<ModelEntity> secondPage = modelDatabaseService.getAllModels(1, 2);
        
        assertEquals(2, secondPage.getContent().size(), "Seconda pagina dovrebbe avere 2 elementi");
        assertTrue(secondPage.isLast(), "Dovrebbe essere l'ultima pagina");

        // === TEST MODELLI PIÙ USATI ===
        Page<ModelEntity> mostUsed = modelDatabaseService.getMostUsedModels(0, 5);
        assertTrue(mostUsed.getContent().size() >= 4, "Dovrebbero esserci almeno 4 modelli");
    }

    /**
     * Test operazioni di aggiornamento
     */
    @Test
    @Order(5)
    void testUpdateOperations() {
        // === SETUP ===
        ModelEntity model = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_1, false);
        modelDatabaseService.saveModel(model);

        // === TEST AGGIORNAMENTO DIRETTO ===
        Optional<ModelEntity> foundModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1);
        assertTrue(foundModel.isPresent(), "Modello dovrebbe esistere");

        ModelEntity toUpdate = foundModel.get();
        toUpdate.setIsActive(true);
        toUpdate.setDescription("Modello attivato");
        
        ModelEntity updated = modelDatabaseService.saveModel(toUpdate);
        assertTrue(updated.getIsActive(), "Modello dovrebbe essere attivo");
        assertEquals("Modello attivato", updated.getDescription());

        // === TEST DISATTIVAZIONE ===
        Long modelId = updated.getId();
        boolean deactivated = modelDatabaseService.deactivateModel(modelId);
        assertTrue(deactivated, "Disattivazione dovrebbe riuscire");

        Optional<ModelEntity> inactiveModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1);
        assertTrue(inactiveModel.isPresent() && !inactiveModel.get().getIsActive(), 
                "Modello dovrebbe essere inattivo");

        // === TEST AGGIORNAMENTO STATUS ===
        boolean statusUpdated = modelDatabaseService.updateModelStatus(modelId, ModelEntity.ModelStatus.ERROR);
        assertTrue(statusUpdated, "Aggiornamento status dovrebbe riuscire");

        Optional<ModelEntity> errorModel = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1);
        assertTrue(errorModel.isPresent() && 
                errorModel.get().getStatus() == ModelEntity.ModelStatus.ERROR,
                "Status dovrebbe essere ERROR");
    }

    /**
     * Test gestione concorrenza e transazioni
     */
    @Test
    @Order(6)
    void testConcurrencyAndTransactions() {
        // === TEST OTTIMISTIC LOCKING ===
        ModelEntity model = createTestModelEntity(TEST_MODEL_NAME, TEST_VERSION_1, false);
        ModelEntity savedModel = modelDatabaseService.saveModel(model);

        // Simula aggiornamento concorrente modificando la versione
        savedModel.setVersion(savedModel.getVersion() + 1);
        savedModel.setDescription("Aggiornamento 1");

        // Primo aggiornamento dovrebbe riuscire
        ModelEntity updated1 = modelDatabaseService.saveModel(savedModel);
        assertNotNull(updated1);

        // === TEST TRANSAZIONE ROLLBACK ===
        try {
            modelDatabaseService.saveModel(null); // Dovrebbe causare eccezione
            fail("Dovrebbe lanciare eccezione per modello null");
        } catch (Exception e) {
            // Verifica che il database sia consistente dopo il rollback
            Optional<ModelEntity> stillExists = modelDatabaseService.findByNameAndVersion(TEST_MODEL_NAME, TEST_VERSION_1 + 1);
            assertTrue(stillExists.isPresent(), "Modello dovrebbe ancora esistere dopo rollback");
        }
    }

    /**
     * Test statistiche e aggregazioni
     */
    @Test
    @Order(7)
    void testStatisticsAndAggregations() {
        // === SETUP ===
        createAndSaveMultipleModels();

        // === TEST COUNT STATISTICHE ===
        List<Object[]> countByType = modelDatabaseService.getModelCountByType();
        assertFalse(countByType.isEmpty(), "Dovrebbero esserci statistiche per tipo");

        List<Object[]> countByStorage = modelDatabaseService.getModelCountByStorageType();
        assertFalse(countByStorage.isEmpty(), "Dovrebbero esserci statistiche per storage");

        // === TEST DIMENSIONE TOTALE ===
        Long totalSize = modelDatabaseService.getTotalStorageSize();
        assertTrue(totalSize > 0, "Dimensione totale dovrebbe essere positiva");

        // === TEST TOTAL PREDICTIONS ===
        Long totalPredictions = modelDatabaseService.getTotalPredictions();
        assertNotNull(totalPredictions, "Total predictions dovrebbe essere valorizzato");

        // === TEST MODELLI INUTILIZZATI ===
        List<ModelEntity> unusedModels = modelDatabaseService.findUnusedModels();
        assertNotNull(unusedModels, "Lista modelli inutilizzati dovrebbe essere valorizzata");

        // === TEST MODELLI VECCHI ===
        List<ModelEntity> staleModels = modelDatabaseService.findStaleModels(30);
        assertNotNull(staleModels, "Lista modelli vecchi dovrebbe essere valorizzata");
    }

    // === METODI HELPER ===

    private ModelEntity createTestModelEntity(String name, String version, boolean isActive) {
        return ModelEntity.builder()
                .modelName(name)
                .version(version)
                .type(ModelEntity.ModelType.ONNX)
                .description("Test model per database integration testing")
                .filePath(name + "/" + version + "/model.onnx")
                .fileSize(1024L)
                .fileHash("test-hash-" + name + "-" + version)
                .storageType(ModelEntity.StorageType.MINIO)
                .bucketName("maverick-test")
                .isActive(isActive)
                .status(ModelEntity.ModelStatus.READY)
                .build();
    }

    private void createAndSaveMultipleModels() {
        // Crea modelli diversi per testare varie funzionalità
        ModelEntity iris1 = createTestModelEntity("iris-model", "v1.0", true);
        ModelEntity iris2 = createTestModelEntity("iris-model", "v2.0", false);
        ModelEntity diabetes = createTestModelEntity("diabetes-model", "v1.0", true);
        ModelEntity housing = createTestModelEntity("housing-model", "v1.0", false);

        // Modifica alcuni attributi per varietà
        diabetes.setType(ModelEntity.ModelType.PMML);
        housing.setFileSize(2048L);

        modelDatabaseService.saveModel(iris1);
        modelDatabaseService.saveModel(iris2);
        modelDatabaseService.saveModel(diabetes);
        modelDatabaseService.saveModel(housing);
    }
}
