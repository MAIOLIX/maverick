package com.maiolix.maverick.exception;

/**
 * Exception thrown when MOJO prediction operations fail
 */
public class MojoPredictionException extends RuntimeException {
    public MojoPredictionException(String message) {
        super(message);
    }
    
    public MojoPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
