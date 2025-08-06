package com.maiolix.maverick.exception;

/**
 * Eccezione generica per operazioni di storage
 * Utilizzata per tutti gli errori relativi ai provider di storage (MinIO, Azure, AWS S3, ecc.)
 */
public class StorageOperationException extends RuntimeException {

    private static final String UNKNOWN_PROVIDER = "UNKNOWN";
    
    private final String providerType;

    /**
     * Costruttore con messaggio
     */
    public StorageOperationException(String message) {
        super(message);
        this.providerType = UNKNOWN_PROVIDER;
    }

    /**
     * Costruttore con messaggio e tipo provider
     */
    public StorageOperationException(String message, String providerType) {
        super(message);
        this.providerType = providerType;
    }

    /**
     * Costruttore con messaggio e causa
     */
    public StorageOperationException(String message, Throwable cause) {
        super(message, cause);
        this.providerType = UNKNOWN_PROVIDER;
    }

    /**
     * Costruttore con messaggio, causa e tipo provider
     */
    public StorageOperationException(String message, Throwable cause, String providerType) {
        super(message, cause);
        this.providerType = providerType;
    }

    /**
     * Costruttore con solo causa
     */
    public StorageOperationException(Throwable cause) {
        super(cause);
        this.providerType = UNKNOWN_PROVIDER;
    }

    /**
     * Restituisce il tipo di provider che ha generato l'errore
     */
    public String getProviderType() {
        return providerType;
    }

    @Override
    public String getMessage() {
        if (!UNKNOWN_PROVIDER.equals(providerType)) {
            return String.format("[%s] %s", providerType, super.getMessage());
        }
        return super.getMessage();
    }
}
