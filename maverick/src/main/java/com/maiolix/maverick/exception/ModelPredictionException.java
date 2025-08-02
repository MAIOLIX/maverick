package com.maiolix.maverick.exception;

/**
 * Exception thrown when a model prediction operation fails
 */
public class ModelPredictionException extends RuntimeException {
    public ModelPredictionException(String message) {
        super(message);
    }
    
    public ModelPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
