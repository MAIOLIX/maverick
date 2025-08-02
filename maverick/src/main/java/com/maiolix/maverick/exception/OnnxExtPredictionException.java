package com.maiolix.maverick.exception;

/**
 * Exception thrown when ONNX Extension prediction operations fail
 */
public class OnnxExtPredictionException extends RuntimeException {
    public OnnxExtPredictionException(String message) {
        super(message);
    }
    
    public OnnxExtPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
