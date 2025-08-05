package com.maiolix.maverick.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.maiolix.maverick.entity.ModelEntity;

/**
 * Repository JPA per la gestione dei modelli ML con supporto PostgreSQL
 */
@Repository
public interface ModelRepository extends JpaRepository<ModelEntity, Long> {

    // ==== QUERY DI RICERCA BASE ====

    /**
     * Trova un modello per nome e versione
     */
    Optional<ModelEntity> findByModelNameAndVersion(String modelName, String version);

    /**
     * Trova un modello per UUID
     */
    Optional<ModelEntity> findByModelUuid(String modelUuid);

    /**
     * Trova tutti i modelli attivi
     */
    List<ModelEntity> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Trova modelli per tipo
     */
    List<ModelEntity> findByTypeAndIsActiveTrue(ModelEntity.ModelType type);

    /**
     * Trova modelli per stato
     */
    List<ModelEntity> findByStatusAndIsActiveTrue(ModelEntity.ModelStatus status);

    // ==== QUERY PER STORAGE TYPE ====

    /**
     * Trova modelli per tipo di storage
     */
    List<ModelEntity> findByStorageTypeAndIsActiveTrue(ModelEntity.StorageType storageType);

    /**
     * Trova modelli cloud (non LOCAL)
     */
    @Query("SELECT m FROM ModelEntity m WHERE m.storageType != 'LOCAL' AND m.isActive = true ORDER BY m.createdAt DESC")
    List<ModelEntity> findCloudModels();

    /**
     * Trova modelli per bucket
     */
    List<ModelEntity> findByBucketNameAndIsActiveTrue(String bucketName);

    /**
     * Trova modelli per bucket e regione
     */
    List<ModelEntity> findByBucketNameAndBucketRegionAndIsActiveTrue(String bucketName, String bucketRegion);

    // ==== QUERY DI RICERCA AVANZATA ====

    /**
     * Ricerca modelli per nome (case-insensitive, LIKE)
     */
    @Query("SELECT m FROM ModelEntity m WHERE LOWER(m.modelName) LIKE LOWER(CONCAT('%', :name, '%')) AND m.isActive = true ORDER BY m.createdAt DESC")
    Page<ModelEntity> searchByModelName(@Param("name") String name, Pageable pageable);

    /**
     * Ricerca full-text nel nome e descrizione
     */
    @Query("SELECT m FROM ModelEntity m WHERE " +
           "(LOWER(m.modelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND m.isActive = true ORDER BY m.createdAt DESC")
    Page<ModelEntity> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Trova modelli usati recentemente
     */
    @Query("SELECT m FROM ModelEntity m WHERE m.lastUsedAt >= :since AND m.isActive = true ORDER BY m.lastUsedAt DESC")
    List<ModelEntity> findRecentlyUsed(@Param("since") LocalDateTime since);

    /**
     * Trova modelli più utilizzati
     */
    @Query("SELECT m FROM ModelEntity m WHERE m.isActive = true ORDER BY m.predictionCount DESC")
    Page<ModelEntity> findMostUsed(Pageable pageable);

    // ==== QUERY DI STATISTICHE ====

    /**
     * Conta modelli per tipo
     */
    @Query("SELECT m.type, COUNT(m) FROM ModelEntity m WHERE m.isActive = true GROUP BY m.type")
    List<Object[]> countByType();

    /**
     * Conta modelli per storage type
     */
    @Query("SELECT m.storageType, COUNT(m) FROM ModelEntity m WHERE m.isActive = true GROUP BY m.storageType")
    List<Object[]> countByStorageType();

    /**
     * Somma totale predizioni
     */
    @Query("SELECT COALESCE(SUM(m.predictionCount), 0) FROM ModelEntity m WHERE m.isActive = true")
    Long getTotalPredictions();

    /**
     * Calcola dimensione totale storage
     */
    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM ModelEntity m WHERE m.isActive = true")
    Long getTotalStorageSize();

    /**
     * Calcola dimensione storage per tipo
     */
    @Query("SELECT m.storageType, COALESCE(SUM(m.fileSize), 0) FROM ModelEntity m WHERE m.isActive = true GROUP BY m.storageType")
    List<Object[]> getStorageSizeByType();

    // ==== OPERAZIONI DI AGGIORNAMENTO ====

    /**
     * Incrementa contatore predizioni
     */
    @Modifying
    @Transactional
    @Query("UPDATE ModelEntity m SET m.predictionCount = COALESCE(m.predictionCount, 0) + 1, m.lastUsedAt = :now WHERE m.id = :id")
    int incrementPredictionCount(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * Aggiorna stato modello
     */
    @Modifying
    @Transactional
    @Query("UPDATE ModelEntity m SET m.status = :status, m.updatedAt = :now WHERE m.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") ModelEntity.ModelStatus status, @Param("now") LocalDateTime now);

    /**
     * Disattiva modello (soft delete)
     */
    @Modifying
    @Transactional
    @Query("UPDATE ModelEntity m SET m.isActive = false, m.updatedAt = :now WHERE m.id = :id")
    int deactivateModel(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * Disattiva tutte le versioni precedenti di un modello
     */
    @Modifying
    @Transactional
    @Query("UPDATE ModelEntity m SET m.isActive = false, m.updatedAt = :now WHERE m.modelName = :modelName AND m.version != :currentVersion")
    int deactivatePreviousVersions(@Param("modelName") String modelName, @Param("currentVersion") String currentVersion, @Param("now") LocalDateTime now);

    // ==== QUERY DI MANUTENZIONE ====

    /**
     * Trova modelli mai utilizzati
     */
    @Query("SELECT m FROM ModelEntity m WHERE m.predictionCount = 0 AND m.isActive = true ORDER BY m.createdAt ASC")
    List<ModelEntity> findUnusedModels();

    /**
     * Trova modelli non utilizzati da X giorni
     */
    @Query("SELECT m FROM ModelEntity m WHERE m.lastUsedAt < :threshold AND m.isActive = true ORDER BY m.lastUsedAt ASC")
    List<ModelEntity> findStaleModels(@Param("threshold") LocalDateTime threshold);

    /**
     * Trova modelli con errori
     */
    List<ModelEntity> findByStatusIn(List<ModelEntity.ModelStatus> statuses);

    /**
     * Trova duplicati (stesso nome, versioni diverse)
     */
    @Query("SELECT m.modelName, COUNT(m) FROM ModelEntity m WHERE m.isActive = true GROUP BY m.modelName HAVING COUNT(m) > 1")
    List<Object[]> findDuplicateModels();

    // ==== QUERY PERSONALIZZATE PER BUCKET ====

    /**
     * Verifica esistenza bucket per storage type
     */
    @Query("SELECT COUNT(m) > 0 FROM ModelEntity m WHERE m.bucketName = :bucketName AND m.storageType = :storageType AND m.isActive = true")
    boolean existsByBucketAndStorageType(@Param("bucketName") String bucketName, @Param("storageType") ModelEntity.StorageType storageType);

    /**
     * Lista bucket utilizzati per storage type
     */
    @Query("SELECT DISTINCT m.bucketName FROM ModelEntity m WHERE m.storageType = :storageType AND m.bucketName IS NOT NULL AND m.isActive = true ORDER BY m.bucketName")
    List<String> findBucketsByStorageType(@Param("storageType") ModelEntity.StorageType storageType);

    /**
     * Conta modelli per bucket
     */
    @Query("SELECT m.bucketName, COUNT(m) FROM ModelEntity m WHERE m.bucketName IS NOT NULL AND m.isActive = true GROUP BY m.bucketName ORDER BY COUNT(m) DESC")
    List<Object[]> countModelsByBucket();
}
