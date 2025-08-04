package com.maiolix.maverick.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModelPredictionExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Prediction failed";
        ModelPredictionException exception = new ModelPredictionException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Prediction failed";
        Throwable cause = new IllegalArgumentException("Invalid input");
        ModelPredictionException exception = new ModelPredictionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ModelPredictionException exception = new ModelPredictionException("Test");
        assertInstanceOf(RuntimeException.class, exception);
    }
}
