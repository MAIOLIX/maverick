package com.maiolix.maverick.exception;

/**
 * Exception thrown when MOJO model operations fail
 */
public class MojoModelException extends RuntimeException {
    public MojoModelException(String message) {
        super(message);
    }
    
    public MojoModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
