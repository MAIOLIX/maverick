package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MojoModelExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "MOJO model loading failed";
        MojoModelException exception = new MojoModelException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "MOJO model loading failed";
        Throwable cause = new RuntimeException("Invalid MOJO file");
        MojoModelException exception = new MojoModelException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        MojoModelException exception = new MojoModelException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
