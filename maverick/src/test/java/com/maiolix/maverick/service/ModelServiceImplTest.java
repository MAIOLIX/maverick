package com.maiolix.maverick.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.registry.ModelRegistry;

class ModelServiceImplTest {

    private ModelServiceImpl modelService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        modelService = new ModelServiceImpl();
        ModelRegistry.clear(); // Clear registry before each test
    }

    @AfterEach
    void tearDown() throws Exception {
        ModelRegistry.clear(); // Clean up after each test
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testUploadModelWithNullFile() {
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(null, "test-model", "ONNX", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model file cannot be null or empty"));
    }

    @Test
    void testUploadModelWithEmptyModelName() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "", "ONNX", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testUploadModelWithNullType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", null, "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model type cannot be null or empty"));
    }

    @Test
    void testUploadModelWithEmptyVersion() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "ONNX", "");
        });
        
        assertTrue(exception.getMessage().contains("Model version cannot be null or empty"));
    }

    @Test
    void testUploadModelWithEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", new byte[0]);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "ONNX", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model file cannot be null or empty"));
    }

    @Test
    void testUploadModelWithUnsupportedType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.model", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "UNSUPPORTED", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Invalid model type"));
    }

    @Test
    void testPredictWithNullInput() {
        Exception exception = assertThrows(ModelPredictionException.class, () -> {
            modelService.predict("test-model", "1.0", null);
        });
        
        assertTrue(exception.getMessage().contains("Input cannot be null"));
    }

    @Test
    void testPredictWithNonExistentModel() {
        Map<String, Object> input = Map.of("feature1", 1.0);
        
        Exception exception = assertThrows(ModelNotFoundException.class, () -> {
            modelService.predict("non-existent-model", "1.0", input);
        });
        
        assertTrue(exception.getMessage().contains("Model not found"));
    }

    @Test
    void testGetInputSchemaWithNonExistentModel() {
        Exception exception = assertThrows(ModelNotFoundException.class, () -> {
            modelService.getInputSchema("non-existent-model", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model not found"));
    }

    @Test
    void testGetModelInfoWithNonExistentModel() {
        Exception exception = assertThrows(ModelNotFoundException.class, () -> {
            modelService.getModelInfo("non-existent-model", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model not found"));
    }

    @Test
    void testAddModelWithNullName() {
        IModelHandler handler = mock(IModelHandler.class);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel(null, "ONNX", "1.0", handler);
        });
        
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testAddModelWithEmptyName() {
        IModelHandler handler = mock(IModelHandler.class);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel("", "ONNX", "1.0", handler);
        });
        
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testAddModelWithNullType() {
        IModelHandler handler = mock(IModelHandler.class);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel("test-model", null, "1.0", handler);
        });
        
        assertTrue(exception.getMessage().contains("Model type cannot be null or empty"));
    }

    @Test
    void testAddModelWithNullVersion() {
        IModelHandler handler = mock(IModelHandler.class);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel("test-model", "ONNX", null, handler);
        });
        
        assertTrue(exception.getMessage().contains("Model version cannot be null or empty"));
    }

    @Test
    void testAddModelWithNullHandler() {
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel("test-model", "ONNX", "1.0", null);
        });
        
        assertTrue(exception.getMessage().contains("Model handler cannot be null"));
    }

    @Test
    void testAddModelWithInvalidHandler() {
        String invalidHandler = "not-a-handler";
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.addModel("test-model", "ONNX", "1.0", invalidHandler);
        });
        
        assertTrue(exception.getMessage().contains("Error adding model"));
    }

    @Test
    void testAddModelSuccess() {
        IModelHandler handler = mock(IModelHandler.class);
        
        assertDoesNotThrow(() -> {
            modelService.addModel("test-model", "ONNX", "1.0", handler);
        });
        
        // Verify model was added to registry
        assertTrue(ModelRegistry.exists("test-model", "1.0"));
    }

    @Test
    void testRemoveModelWithNullName() {
        Exception exception = assertThrows(ModelPredictionException.class, () -> {
            modelService.removeModel(null, "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testRemoveModelWithEmptyName() {
        Exception exception = assertThrows(ModelPredictionException.class, () -> {
            modelService.removeModel("", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testRemoveModelWithNullVersion() {
        Exception exception = assertThrows(ModelPredictionException.class, () -> {
            modelService.removeModel("test-model", null);
        });
        
        assertTrue(exception.getMessage().contains("Model version cannot be null or empty"));
    }

    @Test
    void testRemoveModelNonExistent() {
        boolean result = modelService.removeModel("non-existent-model", "1.0");
        
        assertFalse(result);
    }

    @Test
    void testRemoveModelSuccess() {
        // First add a model
        IModelHandler handler = mock(IModelHandler.class);
        modelService.addModel("test-model", "ONNX", "1.0", handler);
        
        // Verify it exists
        assertTrue(ModelRegistry.exists("test-model", "1.0"));
        
        // Remove it
        boolean result = modelService.removeModel("test-model", "1.0");
        
        // Verify removal
        assertTrue(result);
        assertFalse(ModelRegistry.exists("test-model", "1.0"));
    }

    @Test
    void testGetAllModelsEmpty() {
        Object result = modelService.getAllModels();
        
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result;
        assertEquals(0, response.get("totalModels"));
        
        @SuppressWarnings("unchecked")
        var models = (java.util.List<Map<String, Object>>) response.get("models");
        assertTrue(models.isEmpty());
    }

    @Test
    void testGetAllModelsWithData() {
        // Add some test models
        IModelHandler handler1 = mock(IModelHandler.class);
        IModelHandler handler2 = mock(IModelHandler.class);
        
        modelService.addModel("model-a", "ONNX", "1.0", handler1);
        modelService.addModel("model-b", "MOJO", "2.0", handler2);
        
        Object result = modelService.getAllModels();
        
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result;
        assertEquals(2, response.get("totalModels"));
        
        @SuppressWarnings("unchecked")
        var models = (java.util.List<Map<String, Object>>) response.get("models");
        assertEquals(2, models.size());
        
        // Verify sorting (should be sorted by model name)
        assertEquals("model-a", models.get(0).get("modelName"));
        assertEquals("ONNX", models.get(0).get("type"));
        assertEquals("1.0", models.get(0).get("version"));
        assertFalse((Boolean) models.get(0).get("hasLabelMapping"));
        
        assertEquals("model-b", models.get(1).get("modelName"));
        assertEquals("MOJO", models.get(1).get("type"));
        assertEquals("2.0", models.get(1).get("version"));
        assertFalse((Boolean) models.get(1).get("hasLabelMapping"));
    }

    @Test
    void testGetAllModelsWithSorting() {
        // Add models in different order to test sorting
        IModelHandler handler1 = mock(IModelHandler.class);
        IModelHandler handler2 = mock(IModelHandler.class);
        IModelHandler handler3 = mock(IModelHandler.class);
        
        modelService.addModel("z-model", "ONNX", "1.0", handler1);
        modelService.addModel("a-model", "MOJO", "2.0", handler2);
        modelService.addModel("a-model", "MOJO", "1.0", handler3);
        
        Object result = modelService.getAllModels();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) result;
        assertEquals(3, response.get("totalModels"));
        
        @SuppressWarnings("unchecked")
        var models = (java.util.List<Map<String, Object>>) response.get("models");
        assertEquals(3, models.size());
        
        // Verify sorting: first by model name, then by version
        assertEquals("a-model", models.get(0).get("modelName"));
        assertEquals("1.0", models.get(0).get("version"));
        
        assertEquals("a-model", models.get(1).get("modelName"));
        assertEquals("2.0", models.get(1).get("version"));
        
        assertEquals("z-model", models.get(2).get("modelName"));
        assertEquals("1.0", models.get(2).get("version"));
    }
}
