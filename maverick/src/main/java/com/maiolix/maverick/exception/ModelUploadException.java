package com.maiolix.maverick.exception;

/**
 * Exception thrown when a model upload operation fails
 */
public class ModelUploadException extends RuntimeException {
    public ModelUploadException(String message) {
        super(message);
    }
    
    public ModelUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
