package com.maiolix.maverick.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.registry.ModelRegistry;
import com.maiolix.maverick.repository.MinioModelRepository;

/**
 * Test per ModelBootstrapService
 * Testa il caricamento automatico dei modelli attivi all'avvio
 */
@ExtendWith(MockitoExtension.class)
class ModelBootstrapServiceTest {

    @Mock
    private ModelDatabaseService modelDatabaseService;

    @Mock
    private MinioModelRepository minioRepository;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks
    private ModelBootstrapService bootstrapService;

    @BeforeEach
    void setUp() {
        // Pulisce la cache prima di ogni test
        ModelRegistry.clear();
    }

    /**
     * Test caricamento automatico all'avvio dell'applicazione
     */
    @Test
    void testOnApplicationReady() {
        // === SETUP MOCKS ===
        List<ModelEntity> activeModels = Arrays.asList(
                createTestModel("iris-model", "v1.0"),
                createTestModel("diabetes-model", "v2.0"),
                createTestModel("housing-model", "v1.5")
        );

        when(modelDatabaseService.findActiveModels()).thenReturn(activeModels);

        // Mock successful MinIO downloads
        activeModels.forEach(model -> {
            try {
                String objectPath = model.getFilePath();
                InputStream mockStream = new ByteArrayInputStream("mock model content".getBytes());
                when(minioRepository.downloadModel(objectPath)).thenReturn(mockStream);
            } catch (Exception e) {
                // Ignore setup exceptions
            }
        });

        // === ESECUZIONE ===
        bootstrapService.loadActiveModelsOnStartup();

        // === VERIFICA ===
        verify(modelDatabaseService).findActiveModels();
        
        // Verifica che tutti i modelli attivi siano stati caricati
        activeModels.forEach(model -> {
            assertTrue(ModelRegistry.exists(model.getModelName(), model.getVersion()),
                    "Modello " + model.getModelName() + " v" + model.getVersion() + " dovrebbe essere caricato");
        });

        // Verifica che il registry contenga esattamente 3 modelli
        assertEquals(3, ModelRegistry.size(),
                "Registry dovrebbe contenere 3 modelli caricati");
    }

    /**
     * Test caricamento con alcuni modelli che falliscono
     */
    @Test
    void testOnApplicationReadyWithFailures() {
        // === SETUP MOCKS ===
        List<ModelEntity> activeModels = Arrays.asList(
                createTestModel("working-model", "v1.0"),
                createTestModel("failing-model", "v1.0"),
                createTestModel("another-working-model", "v2.0")
        );

        when(modelDatabaseService.findActiveModels()).thenReturn(activeModels);

        // Mock: primo e terzo modello riescono, secondo fallisce
        try {
            InputStream mockStream1 = new ByteArrayInputStream("mock content 1".getBytes());
            when(minioRepository.downloadModel("working-model/v1.0/model.onnx")).thenReturn(mockStream1);

            when(minioRepository.downloadModel("failing-model/v1.0/model.onnx"))
                    .thenThrow(new RuntimeException("MinIO download failed"));

            InputStream mockStream3 = new ByteArrayInputStream("mock content 3".getBytes());
            when(minioRepository.downloadModel("another-working-model/v2.0/model.onnx")).thenReturn(mockStream3);
        } catch (Exception e) {
            // Ignore setup exceptions
        }

        // === ESECUZIONE ===
        bootstrapService.loadActiveModelsOnStartup();

        // === VERIFICA ===
        // Verifica che i modelli funzionanti siano stati caricati
        assertTrue(ModelRegistry.exists("working-model", "v1.0"),
                "working-model dovrebbe essere caricato");
        assertTrue(ModelRegistry.exists("another-working-model", "v2.0"),
                "another-working-model dovrebbe essere caricato");

        // Verifica che il modello fallito non sia stato caricato
        assertFalse(ModelRegistry.exists("failing-model", "v1.0"),
                "failing-model non dovrebbe essere caricato");

        // Verifica che il registry contenga solo 2 modelli (quelli che funzionano)
        assertEquals(2, ModelRegistry.size(),
                "Registry dovrebbe contenere 2 modelli caricati");
    }

    /**
     * Test quando non ci sono modelli attivi
     */
    @Test
    void testOnApplicationReadyWithNoActiveModels() {
        // === SETUP MOCKS ===
        when(modelDatabaseService.findActiveModels()).thenReturn(Arrays.asList());

        // === ESECUZIONE ===
        bootstrapService.loadActiveModelsOnStartup();

        // === VERIFICA ===
        verify(modelDatabaseService).findActiveModels();
        assertEquals(0, ModelRegistry.size(),
                "Registry dovrebbe essere vuoto quando non ci sono modelli attivi");
    }

    /**
     * Test quando il database service fallisce
     */
    @Test
    void testOnApplicationReadyWithDatabaseFailure() {
        // === SETUP MOCKS ===
        when(modelDatabaseService.findActiveModels())
                .thenThrow(new RuntimeException("Database connection failed"));

        // === ESECUZIONE ===
        // Il servizio dovrebbe gestire l'eccezione senza propagarla
        assertDoesNotThrow(() -> {
            bootstrapService.loadActiveModelsOnStartup();
        }, "Bootstrap dovrebbe gestire i fallimenti del database senza propagare eccezioni");

        // === VERIFICA ===
        assertEquals(0, ModelRegistry.size(),
                "Registry dovrebbe essere vuoto quando il database fallisce");
    }

    /**
     * Test reload manuale
     */
    @Test
    void testManualReload() {
        // === SETUP INIZIALE ===
        // Simula alcuni modelli già caricati
        ModelRegistry.clear();
        
        // === SETUP MOCKS PER RELOAD ===
        List<ModelEntity> activeModels = Arrays.asList(
                createTestModel("new-model", "v1.0"),
                createTestModel("updated-model", "v2.0")
        );

        when(modelDatabaseService.findActiveModels()).thenReturn(activeModels);

        // Mock successful downloads
        try {
            InputStream mockStream1 = new ByteArrayInputStream("new model content".getBytes());
            when(minioRepository.downloadModel("new-model/v1.0/model.onnx")).thenReturn(mockStream1);

            InputStream mockStream2 = new ByteArrayInputStream("updated model content".getBytes());
            when(minioRepository.downloadModel("updated-model/v2.0/model.onnx")).thenReturn(mockStream2);
        } catch (Exception e) {
            // Ignore setup exceptions
        }

        // === ESECUZIONE RELOAD ===
        bootstrapService.reloadAllActiveModels();

        // === VERIFICA ===
        assertTrue(ModelRegistry.exists("new-model", "v1.0"),
                "new-model dovrebbe essere caricato dopo reload");
        assertTrue(ModelRegistry.exists("updated-model", "v2.0"),
                "updated-model dovrebbe essere caricato dopo reload");
        assertEquals(2, ModelRegistry.size(),
                "Registry dovrebbe contenere 2 modelli dopo reload");
    }

    /**
     * Test con modelli di tipi diversi
     */
    @Test
    void testLoadingDifferentModelTypes() {
        // === SETUP MOCKS ===
        List<ModelEntity> activeModels = Arrays.asList(
                createTestModelWithType("onnx-model", "v1.0", ModelEntity.ModelType.ONNX),
                createTestModelWithType("pmml-model", "v1.0", ModelEntity.ModelType.PMML)
        );

        when(modelDatabaseService.findActiveModels()).thenReturn(activeModels);

        // Mock downloads for all model types
        try {
            for (ModelEntity model : activeModels) {
                InputStream mockStream = new ByteArrayInputStream("mock content".getBytes());
                when(minioRepository.downloadModel(model.getFilePath())).thenReturn(mockStream);
            }
        } catch (Exception e) {
            // Ignore setup exceptions
        }

        // === ESECUZIONE ===
        bootstrapService.loadActiveModelsOnStartup();

        // === VERIFICA ===
        // Verifica che tutti i tipi di modello siano stati caricati
        assertTrue(ModelRegistry.exists("onnx-model", "v1.0"));
        assertTrue(ModelRegistry.exists("pmml-model", "v1.0"));
        assertEquals(2, ModelRegistry.size());
    }

    /**
     * Test gestione memoria e performance
     */
    @Test
    void testMemoryAndPerformance() {
        // === SETUP MOLTI MODELLI ===
        List<ModelEntity> activeModels = Arrays.asList(
                createTestModel("model-1", "v1.0"),
                createTestModel("model-2", "v1.0"),
                createTestModel("model-3", "v1.0"),
                createTestModel("model-4", "v1.0"),
                createTestModel("model-5", "v1.0")
        );

        when(modelDatabaseService.findActiveModels()).thenReturn(activeModels);

        // Mock downloads
        try {
            for (ModelEntity model : activeModels) {
                InputStream mockStream = new ByteArrayInputStream("large mock content".getBytes());
                when(minioRepository.downloadModel(model.getFilePath())).thenReturn(mockStream);
            }
        } catch (Exception e) {
            // Ignore setup exceptions
        }

        // === ESECUZIONE CON MISURAZIONE TEMPO ===
        long startTime = System.currentTimeMillis();
        bootstrapService.loadActiveModelsOnStartup();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // === VERIFICA ===
        assertEquals(5, ModelRegistry.size(),
                "Tutti i 5 modelli dovrebbero essere caricati");

        assertTrue(executionTime < 5000,
                "Caricamento di 5 modelli dovrebbe completare in meno di 5 secondi");

        System.out.println("Caricamento di " + activeModels.size() + 
                " modelli completato in " + executionTime + "ms");
    }

    // === METODI HELPER ===

    private ModelEntity createTestModel(String name, String version) {
        return createTestModelWithType(name, version, ModelEntity.ModelType.ONNX);
    }

    private ModelEntity createTestModelWithType(String name, String version, ModelEntity.ModelType type) {
        return ModelEntity.builder()
                .modelName(name)
                .version(version)
                .type(type)
                .description("Test model for bootstrap testing")
                .filePath(name + "/" + version + "/model." + type.name().toLowerCase())
                .fileSize(1024L)
                .fileHash("test-hash-" + name)
                .storageType(ModelEntity.StorageType.MINIO)
                .bucketName("maverick-test")
                .isActive(true)
                .status(ModelEntity.ModelStatus.READY)
                .build();
    }
}
