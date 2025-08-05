package com.maiolix.maverick.exception;

/**
 * Eccezione personalizzata per operazioni MinIO
 */
public class MinioOperationException extends RuntimeException {

    public MinioOperationException(String message) {
        super(message);
    }

    public MinioOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioOperationException(Throwable cause) {
        super(cause);
    }
}
