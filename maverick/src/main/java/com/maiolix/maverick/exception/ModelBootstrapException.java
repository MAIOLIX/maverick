package com.maiolix.maverick.exception;

/**
 * Eccezione specifica per errori durante il bootstrap dei modelli
 * Utilizzata quando si verificano errori durante il caricamento automatico dei modelli all'avvio
 */
public class ModelBootstrapException extends RuntimeException {

    /**
     * Costruttore con messaggio
     */
    public ModelBootstrapException(String message) {
        super(message);
    }

    /**
     * Costruttore con messaggio e causa
     */
    public ModelBootstrapException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Costruttore con solo causa
     */
    public ModelBootstrapException(Throwable cause) {
        super(cause);
    }
}
