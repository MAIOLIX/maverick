package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnnxPredictionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "ONNX prediction failed";
        OnnxPredictionException exception = new OnnxPredictionException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "ONNX prediction failed";
        Throwable cause = new IllegalArgumentException("Invalid tensor shape");
        OnnxPredictionException exception = new OnnxPredictionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        OnnxPredictionException exception = new OnnxPredictionException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
