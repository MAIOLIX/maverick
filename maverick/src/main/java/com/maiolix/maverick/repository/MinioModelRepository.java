package com.maiolix.maverick.repository;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.maiolix.maverick.exception.StorageOperationException;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementazione MinIO del repository per la gestione dei modelli ML
 * Struttura: bucket/modello/versione/file
 */
@Repository
@Slf4j
public class MinioModelRepository implements IModelStorageRepository {

    @Value("${maverick.storage.minio.endpoint}")
    private String endpoint;

    @Value("${maverick.storage.minio.access-key}")
    private String accessKey;

    @Value("${maverick.storage.minio.secret-key}")
    private String secretKey;

    @Value("${maverick.storage.minio.default-bucket}")
    private String defaultBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void initializeMinioClient() {
        try {
            log.info("🔧 Inizializzando MinIO client - Endpoint: {}", endpoint);
            
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Crea il bucket se non esiste
            createBucketIfNotExists(defaultBucket);
            
            log.info("✅ MinIO client inizializzato con successo - Bucket: {}", defaultBucket);
            
        } catch (Exception e) {
            log.error("❌ Errore inizializzazione MinIO: {}", e.getMessage(), e);
            throw new StorageOperationException("Impossibile inizializzare MinIO client", e, "MINIO");
        }
    }

    /**
     * Crea bucket se non esiste
     */
    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("📁 Bucket '{}' creato", bucketName);
            } else {
                log.debug("📁 Bucket '{}' già esistente", bucketName);
            }
        } catch (Exception e) {
            log.error("❌ Errore creazione bucket '{}': {}", bucketName, e.getMessage());
            throw new StorageOperationException("Impossibile creare bucket: " + bucketName, e, "MINIO");
        }
    }

    /**
     * Carica un modello nel bucket con struttura modello/versione/
     * 
     * @param modelName Nome del modello
     * @param version Versione del modello
     * @param fileName Nome del file
     * @param inputStream Stream del file
     * @param fileSize Dimensione del file
     * @param contentType Tipo MIME del file
     */
    public void uploadModel(String modelName, String version, String fileName, 
                           InputStream inputStream, long fileSize, String contentType) {
        try {
            // Genera il path: modello/versione/file
            String objectPath = generateModelPath(modelName, version, fileName);
            
            log.info("📤 Caricamento modello: {} -> {}", modelName + "/" + version, objectPath);
            
            // MinIO crea automaticamente la struttura di cartelle tramite il path dell'oggetto
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectPath)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            
            log.info("✅ Modello caricato con successo: {}", objectPath);
            
        } catch (Exception e) {
            log.error("❌ Errore upload modello {}/{}: {}", modelName, version, e.getMessage(), e);
            throw new StorageOperationException("Upload modello fallito: " + modelName + "/" + version, e, "MINIO");
        }
    }

    /**
     * Genera il path strutturato per il modello
     * Formato: modello/versione/file
     */
    private String generateModelPath(String modelName, String version, String fileName) {
        return String.format("%s/%s/%s", modelName, version, fileName);
    }

    /**
     * Test della connessione MinIO
     */
    public void testConnection() {
        try {
            minioClient.listBuckets();
            log.info("✅ Connessione MinIO OK");
        } catch (Exception e) {
            log.error("❌ Test connessione MinIO fallito: {}", e.getMessage());
            throw new StorageOperationException("Connessione MinIO fallita", e, "MINIO");
        }
    }

    // Getter utili
    public String getDefaultBucket() {
        return defaultBucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public StorageProviderType getProviderType() {
        return StorageProviderType.MINIO;
    }

    /**
     * Scarica un modello da MinIO
     */
    public InputStream downloadModel(String objectPath) {
        try {
            log.info("📥 Download modello da MinIO: {}", objectPath);
            
            return minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectPath)
                    .build());
                    
        } catch (Exception e) {
            log.error("❌ Errore download modello {}: {}", objectPath, e.getMessage(), e);
            throw new StorageOperationException("Errore download modello: " + e.getMessage(), e, "MINIO");
        }
    }

    /**
     * Elimina un modello da MinIO
     */
    public boolean deleteModel(String objectPath) {
        try {
            log.info("🗑️ Eliminazione modello da MinIO: {}", objectPath);
            
            minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                    .bucket(defaultBucket)
                    .object(objectPath)
                    .build());
                    
            log.info("✅ Modello eliminato da MinIO: {}", objectPath);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Errore eliminazione modello {}: {}", objectPath, e.getMessage(), e);
            return false;
        }
    }
}
