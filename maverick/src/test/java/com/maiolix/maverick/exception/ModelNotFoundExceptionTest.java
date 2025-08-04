package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModelNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Model not found";
        ModelNotFoundException exception = new ModelNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Model not found";
        Throwable cause = new IllegalStateException("Registry is empty");
        ModelNotFoundException exception = new ModelNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ModelNotFoundException exception = new ModelNotFoundException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
