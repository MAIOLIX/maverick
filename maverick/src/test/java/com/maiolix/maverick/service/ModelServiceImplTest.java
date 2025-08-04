package com.maiolix.maverick.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
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
        
        assertTrue(exception.getMessage().contains("File cannot be null"));
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
        
        assertTrue(exception.getMessage().contains("Type cannot be null or empty"));
    }

    @Test
    void testUploadModelWithEmptyVersion() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "ONNX", "");
        });
        
        assertTrue(exception.getMessage().contains("Version cannot be null or empty"));
    }

    @Test
    void testUploadModelWithEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", new byte[0]);
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "ONNX", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("File cannot be empty"));
    }

    @Test
    void testUploadModelWithUnsupportedType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.model", "application/octet-stream", "test content".getBytes());
        
        Exception exception = assertThrows(ModelUploadException.class, () -> {
            modelService.uploadModel(file, "test-model", "UNSUPPORTED", "1.0");
        });
        
        assertTrue(exception.getMessage().contains("Unsupported model type"));
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
}
