package com.maiolix.maverick.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità JPA per la gestione dei modelli ML con supporto multi-storage
 */
@Entity
@Table(name = "models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificazione modello
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "model_uuid", unique = true, length = 36)
    private String modelUuid;

    // Metadati del modello
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ModelType type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Storage location
    @Column(name = "storage_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StorageType storageType = StorageType.LOCAL;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "bucket_name", length = 100)
    private String bucketName;

    @Column(name = "bucket_region", length = 50)
    private String bucketRegion;

    @Column(name = "storage_class", length = 50)
    private String storageClass;

    // Metadati file
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "content_type", length = 100)
    @Builder.Default
    private String contentType = "application/octet-stream";

    // Configurazione (JSON stored as TEXT)
    @Column(name = "input_schema", columnDefinition = "TEXT")
    private String inputSchema;

    @Column(name = "output_schema", columnDefinition = "TEXT")
    private String outputSchema;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Statistiche di utilizzo
    @Column(name = "prediction_count")
    @Builder.Default
    private Long predictionCount = 0L;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Stato e controllo
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ModelStatus status = ModelStatus.READY;

    // Audit trail
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    @Builder.Default
    private String createdBy = "system";

    @Column(name = "updated_by", length = 100)
    @Builder.Default
    private String updatedBy = "system";

    // Constraint univoco per nome e versione
    @Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_models_name_version", columnNames = {"model_name", "version"})
    })
    public static class ModelEntityConstraints {}

    /**
     * Enum per i tipi di modello supportati
     */
    public enum ModelType {
        ONNX, PMML, MOJO, H2O, ONNX_EXT
    }

    /**
     * Enum per i tipi di storage supportati
     */
    public enum StorageType {
        LOCAL, S3, AZURE_BLOB, GCS, MINIO
    }

    /**
     * Enum per lo stato del modello
     */
    public enum ModelStatus {
        READY, LOADING, ERROR, MAINTENANCE
    }

    /**
     * Genera l'URL completo del modello basato sul tipo di storage
     */
    public String getFullUrl() {
        return switch (storageType) {
            case LOCAL -> "file://" + filePath;
            case S3 -> "s3://" + bucketName + "/" + filePath;
            case AZURE_BLOB -> "https://" + bucketName.split("/")[0] + ".blob.core.windows.net/" + filePath;
            case GCS -> "gs://" + bucketName + "/" + filePath;
            case MINIO -> "minio://" + bucketName + "/" + filePath;
            default -> filePath;
        };
    }

    /**
     * Incrementa il contatore delle predizioni
     */
    public void incrementPredictionCount() {
        this.predictionCount = (this.predictionCount == null ? 0L : this.predictionCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Verifica se è un modello cloud-based
     */
    public boolean isCloudStorage() {
        return storageType != StorageType.LOCAL;
    }

    /**
     * Ottiene la dimensione del file in MB
     */
    public double getFileSizeMB() {
        return fileSize != null ? fileSize / (1024.0 * 1024.0) : 0.0;
    }
}
