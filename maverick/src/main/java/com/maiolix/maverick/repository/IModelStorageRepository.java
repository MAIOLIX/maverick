package com.maiolix.maverick.repository;

import java.io.InputStream;

/**
 * Interfaccia astratta per la gestione dello storage dei modelli ML
 * Supporta diversi provider di storage (MinIO, Azure Blob, AWS S3, ecc.)
 */
public interface IModelStorageRepository {

    /**
     * Carica un modello nel storage con struttura modelName/version/fileName
     * 
     * @param modelName Nome del modello
     * @param version Versione del modello
     * @param fileName Nome del file
     * @param inputStream Stream del file
     * @param fileSize Dimensione del file
     * @param contentType Tipo MIME del file
     * @throws StorageOperationException in caso di errore
     */
    void uploadModel(String modelName, String version, String fileName, 
                    InputStream inputStream, long fileSize, String contentType);

    /**
     * Scarica un modello dal storage
     * 
     * @param objectPath Path del modello nel storage (es. modelName/version/fileName)
     * @return InputStream del modello
     * @throws StorageOperationException in caso di errore
     */
    InputStream downloadModel(String objectPath);

    /**
     * Elimina un modello dal storage
     * 
     * @param objectPath Path del modello nel storage (es. modelName/version/fileName)
     * @return true se eliminato con successo, false altrimenti
     * @throws StorageOperationException in caso di errore critico
     */
    boolean deleteModel(String objectPath);

    /**
     * Testa la connessione al storage
     * 
     * @throws StorageOperationException in caso di errore di connessione
     */
    void testConnection();

    /**
     * Restituisce il nome del bucket/container predefinito
     * 
     * @return Nome del bucket/container
     */
    String getDefaultBucket();

    /**
     * Restituisce l'endpoint del storage
     * 
     * @return URL dell'endpoint
     */
    String getEndpoint();

    /**
     * Restituisce il tipo di storage provider
     * 
     * @return Tipo di provider (MINIO, AZURE_BLOB, AWS_S3, ecc.)
     */
    StorageProviderType getProviderType();

    /**
     * Enum per i tipi di storage provider supportati
     */
    enum StorageProviderType {
        MINIO("MinIO"),
        AZURE_BLOB("Azure Blob Storage"),
        AWS_S3("Amazon S3"),
        GOOGLE_CLOUD("Google Cloud Storage"),
        LOCAL_FILE("Local File System");

        private final String displayName;

        StorageProviderType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
