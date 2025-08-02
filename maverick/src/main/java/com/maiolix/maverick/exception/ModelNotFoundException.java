package com.maiolix.maverick.exception;

/**
 * Exception thrown when a requested model is not found
 */
public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String message) {
        super(message);
    }
    
    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
