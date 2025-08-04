package com.maiolix.maverick.controller.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.maiolix.maverick.controller.dto.ErrorResponse;
import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.exception.OnnxModelException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/models/test");
    }

    @Test
    void testHandleModelNotFoundException() {
        ModelNotFoundException exception = new ModelNotFoundException("Model not found: test-model");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleModelNotFoundException(exception, request);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MODEL_NOT_FOUND", response.getBody().getError());
        assertEquals("Model not found: test-model", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void testHandleModelUploadException() {
        ModelUploadException exception = new ModelUploadException("Invalid file format");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleModelUploadException(exception, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MODEL_UPLOAD_ERROR", response.getBody().getError());
        assertEquals("Invalid file format", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Check your model file format and parameters", response.getBody().getDetails());
    }

    @Test
    void testHandleModelPredictionException() {
        ModelPredictionException exception = new ModelPredictionException("Invalid input data");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleModelPredictionException(exception, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MODEL_PREDICTION_ERROR", response.getBody().getError());
        assertEquals("Invalid input data", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Check your input data format and model availability", response.getBody().getDetails());
    }

    @Test
    void testHandleOnnxModelException() {
        OnnxModelException exception = new OnnxModelException("Invalid ONNX model format");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleModelFormatException(exception, request);
        
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ONNX_MODEL_ERROR", response.getBody().getError());
        assertEquals("Invalid ONNX model format", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("The uploaded model file format is not valid or corrupted", response.getBody().getDetails());
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1024L);
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxUploadSizeExceededException(exception, request);
        
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FILE_SIZE_EXCEEDED", response.getBody().getError());
        assertEquals("The uploaded file size exceeds the maximum allowed limit", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(413, response.getBody().getStatus());
        assertEquals("Maximum file size allowed is configured in application properties", response.getBody().getDetails());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().getError());
        assertEquals("Invalid argument provided", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Check the provided parameters and their format", response.getBody().getDetails());
    }

    @Test
    void testHandleGenericException() {
        RuntimeException exception = new RuntimeException("Unexpected error occurred");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/api/v1/models/test", response.getBody().getPath());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Please contact support if the problem persists", response.getBody().getDetails());
    }
}
