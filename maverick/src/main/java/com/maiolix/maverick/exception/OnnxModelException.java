package com.maiolix.maverick.exception;

/**
 * Exception thrown when ONNX model operations fail
 */
public class OnnxModelException extends RuntimeException {
    public OnnxModelException(String message) {
        super(message);
    }
    
    public OnnxModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
