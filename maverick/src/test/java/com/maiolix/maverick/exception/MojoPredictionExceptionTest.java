package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MojoPredictionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "MOJO prediction failed";
        MojoPredictionException exception = new MojoPredictionException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "MOJO prediction failed";
        Throwable cause = new IllegalArgumentException("Missing feature value");
        MojoPredictionException exception = new MojoPredictionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        MojoPredictionException exception = new MojoPredictionException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
