package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnnxExtPredictionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "ONNX Extended prediction failed";
        OnnxExtPredictionException exception = new OnnxExtPredictionException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "ONNX Extended prediction failed";
        Throwable cause = new IllegalArgumentException("Label mapping not found");
        OnnxExtPredictionException exception = new OnnxExtPredictionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        OnnxExtPredictionException exception = new OnnxExtPredictionException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
