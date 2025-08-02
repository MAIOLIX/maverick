package com.maiolix.maverick.exception;

/**
 * Exception thrown when ONNX Extension model operations fail
 */
public class OnnxExtModelException extends RuntimeException {
    public OnnxExtModelException(String message) {
        super(message);
    }
    
    public OnnxExtModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
