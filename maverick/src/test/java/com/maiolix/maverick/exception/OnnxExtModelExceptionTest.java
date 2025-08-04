package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnnxExtModelExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "ONNX Extended model loading failed";
        OnnxExtModelException exception = new OnnxExtModelException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "ONNX Extended model loading failed";
        Throwable cause = new RuntimeException("ZIP extraction failed");
        OnnxExtModelException exception = new OnnxExtModelException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        OnnxExtModelException exception = new OnnxExtModelException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
