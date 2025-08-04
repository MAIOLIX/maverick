package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModelUploadExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Upload failed";
        ModelUploadException exception = new ModelUploadException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Upload failed";
        Throwable cause = new RuntimeException("Root cause");
        ModelUploadException exception = new ModelUploadException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ModelUploadException exception = new ModelUploadException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
