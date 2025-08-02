package com.maiolix.maverick.exception;

/**
 * Exception thrown when ONNX prediction operations fail
 */
public class OnnxPredictionException extends RuntimeException {
    public OnnxPredictionException(String message) {
        super(message);
    }
    
    public OnnxPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
