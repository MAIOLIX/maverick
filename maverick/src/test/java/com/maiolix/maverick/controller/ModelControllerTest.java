package com.maiolix.maverick.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.service.IModelService;

class ModelControllerTest {

    @Mock
    private IModelService modelService;

    private ModelController modelController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        modelController = new ModelController(modelService);
    }

    @Test
    void testUploadModelSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        doNothing().when(modelService).uploadModel(any(), eq("test-model"), eq("ONNX"), eq("1.0"));
        
        ResponseEntity<String> response = modelController.uploadModel(file, "test-model", "ONNX", "1.0");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Model uploaded successfully: test-model version: 1.0"));
        verify(modelService).uploadModel(file, "test-model", "ONNX", "1.0");
    }

    @Test
    void testUploadModelException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.onnx", "application/octet-stream", "test content".getBytes());
        
        doThrow(new ModelUploadException("Invalid file format")).when(modelService).uploadModel(any(), eq("test-model"), eq("ONNX"), eq("1.0"));
        
        // Since exceptions are now handled by GlobalExceptionHandler, this should throw the exception
        assertThrows(ModelUploadException.class, () -> {
            modelController.uploadModel(file, "test-model", "ONNX", "1.0");
        });
        
        verify(modelService).uploadModel(file, "test-model", "ONNX", "1.0");
    }

    @Test
    void testPredictSuccess() {
        Map<String, Object> input = Map.of("feature1", 1.0);
        Map<String, Object> expectedResult = Map.of("prediction", 0.8);
        
        when(modelService.predict("test-model", "1.0", input)).thenReturn(expectedResult);
        
        ResponseEntity<Object> response = modelController.predict("test-model", "1.0", input);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(modelService).predict("test-model", "1.0", input);
    }

    @Test
    void testPredictException() {
        Map<String, Object> input = Map.of("feature1", 1.0);
        
        when(modelService.predict("test-model", "1.0", input)).thenThrow(new ModelNotFoundException("Model not found"));
        
        // Since exceptions are now handled by GlobalExceptionHandler, this should throw the exception
        assertThrows(ModelNotFoundException.class, () -> {
            modelController.predict("test-model", "1.0", input);
        });
    }

    @Test
    void testGetInputSchemaSuccess() {
        Map<String, Object> expectedSchema = Map.of("inputs", Map.of("feature1", "float"));
        
        when(modelService.getInputSchema("test-model", "1.0")).thenReturn(expectedSchema);
        
        ResponseEntity<Object> response = modelController.getInputSchema("test-model", "1.0");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchema, response.getBody());
        verify(modelService).getInputSchema("test-model", "1.0");
    }

    @Test
    void testGetModelInfoSuccess() {
        Map<String, Object> expectedInfo = Map.of("modelType", "ONNX", "version", "1.0");
        
        when(modelService.getModelInfo("test-model", "1.0")).thenReturn(expectedInfo);
        
        ResponseEntity<Object> response = modelController.getModelInfo("test-model", "1.0");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedInfo, response.getBody());
        verify(modelService).getModelInfo("test-model", "1.0");
    }

    @Test
    void testAddModelSuccess() {
        Object mockHandler = mock(Object.class);
        
        doNothing().when(modelService).addModel("test-model", "ONNX", "1.0", mockHandler);
        
        ResponseEntity<String> response = modelController.addModel("test-model", "ONNX", "1.0", mockHandler);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Model added successfully: test-model version: 1.0"));
        verify(modelService).addModel("test-model", "ONNX", "1.0", mockHandler);
    }

    @Test
    void testRemoveModelSuccess() {
        when(modelService.removeModel("test-model", "1.0")).thenReturn(true);
        
        ResponseEntity<String> response = modelController.removeModel("test-model", "1.0");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Model removed successfully: test-model version: 1.0"));
        verify(modelService).removeModel("test-model", "1.0");
    }

    @Test
    void testRemoveModelNotFound() {
        when(modelService.removeModel("test-model", "1.0")).thenReturn(false);
        
        ResponseEntity<String> response = modelController.removeModel("test-model", "1.0");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Model not found: test-model version: 1.0"));
        verify(modelService).removeModel("test-model", "1.0");
    }

    @Test
    void testGetAllModelsSuccess() {
        Map<String, Object> expectedModels = Map.of("totalModels", 2, "models", java.util.List.of());
        
        when(modelService.getAllModels()).thenReturn(expectedModels);
        
        ResponseEntity<Object> response = modelController.getAllModels();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedModels, response.getBody());
        verify(modelService).getAllModels();
    }
}
