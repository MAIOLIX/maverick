package com.maiolix.maverick.handler;

public class MojoPredictionException extends RuntimeException {
    public MojoPredictionException(String message) {
        super(message);
    }
    
    public MojoPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
