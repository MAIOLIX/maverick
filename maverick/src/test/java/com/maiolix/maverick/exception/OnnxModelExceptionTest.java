package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OnnxModelExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "ONNX model loading failed";
        OnnxModelException exception = new OnnxModelException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "ONNX model loading failed";
        Throwable cause = new RuntimeException("File not found");
        OnnxModelException exception = new OnnxModelException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        OnnxModelException exception = new OnnxModelException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
