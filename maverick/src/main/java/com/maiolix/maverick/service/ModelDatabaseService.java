package com.maiolix.maverick.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.repository.ModelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servizio per la gestione dei modelli ML con integrazione PostgreSQL
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModelDatabaseService {

    private final ModelRepository modelRepository;

    // ==== OPERAZIONI CRUD BASE ====

    /**
     * Salva un nuovo modello
     */
    public ModelEntity saveModel(ModelEntity model) {
        if (model.getModelUuid() == null) {
            model.setModelUuid(UUID.randomUUID().toString());
        }
        
        log.info("Salvando modello: {} v{} in {}", 
                model.getModelName(), model.getVersion(), model.getStorageType());
        
        return modelRepository.save(model);
    }

    /**
     * Trova un modello per ID
     */
    @Transactional(readOnly = true)
    public Optional<ModelEntity> findById(Long id) {
        return modelRepository.findById(id);
    }

    /**
     * Trova un modello per nome e versione
     */
    @Transactional(readOnly = true)
    public Optional<ModelEntity> findByNameAndVersion(String name, String version) {
        return modelRepository.findByModelNameAndVersion(name, version);
    }

    /**
     * Trova un modello per UUID
     */
    @Transactional(readOnly = true)
    public Optional<ModelEntity> findByUuid(String uuid) {
        return modelRepository.findByModelUuid(uuid);
    }

    /**
     * Lista tutti i modelli attivi
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findAllActiveModels() {
        return modelRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    // ==== OPERAZIONI DI RICERCA ====

    /**
     * Ricerca modelli per nome
     */
    @Transactional(readOnly = true)
    public Page<ModelEntity> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return modelRepository.searchByModelName(name, pageable);
    }

    /**
     * Ricerca full-text
     */
    @Transactional(readOnly = true)
    public Page<ModelEntity> fullTextSearch(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return modelRepository.fullTextSearch(searchTerm, pageable);
    }

    /**
     * Trova modelli per tipo
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findByType(ModelEntity.ModelType type) {
        return modelRepository.findByTypeAndIsActiveTrue(type);
    }

    /**
     * Trova modelli per storage type
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findByStorageType(ModelEntity.StorageType storageType) {
        return modelRepository.findByStorageTypeAndIsActiveTrue(storageType);
    }

    /**
     * Trova modelli cloud
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findCloudModels() {
        return modelRepository.findCloudModels();
    }

    /**
     * Trova modelli per bucket
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findByBucket(String bucketName) {
        return modelRepository.findByBucketNameAndIsActiveTrue(bucketName);
    }

    // ==== OPERAZIONI DI AGGIORNAMENTO ====

    /**
     * Registra utilizzo modello
     */
    public void recordPrediction(Long modelId) {
        int updated = modelRepository.incrementPredictionCount(modelId, LocalDateTime.now());
        if (updated > 0) {
            log.debug("Incrementato contatore predizioni per modello ID: {}", modelId);
        }
    }

    /**
     * Aggiorna stato modello
     */
    public boolean updateModelStatus(Long modelId, ModelEntity.ModelStatus status) {
        int updated = modelRepository.updateStatus(modelId, status, LocalDateTime.now());
        if (updated > 0) {
            log.info("Stato modello ID {} aggiornato a: {}", modelId, status);
            return true;
        }
        return false;
    }

    /**
     * Disattiva modello
     */
    public boolean deactivateModel(Long modelId) {
        int updated = modelRepository.deactivateModel(modelId, LocalDateTime.now());
        if (updated > 0) {
            log.info("Modello ID {} disattivato", modelId);
            return true;
        }
        return false;
    }

    /**
     * Deploy di una nuova versione (disattiva le precedenti)
     */
    public ModelEntity deployNewVersion(ModelEntity newModel) {
        // Salva il nuovo modello
        ModelEntity saved = saveModel(newModel);
        
        // Disattiva le versioni precedenti
        int deactivated = modelRepository.deactivatePreviousVersions(
                newModel.getModelName(), 
                newModel.getVersion(), 
                LocalDateTime.now()
        );
        
        log.info("Deployed {} v{}, disattivate {} versioni precedenti", 
                newModel.getModelName(), newModel.getVersion(), deactivated);
        
        return saved;
    }

    // ==== STATISTICHE ====

    /**
     * Conta modelli per tipo
     */
    @Transactional(readOnly = true)
    public List<Object[]> getModelCountByType() {
        return modelRepository.countByType();
    }

    /**
     * Conta modelli per storage type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getModelCountByStorageType() {
        return modelRepository.countByStorageType();
    }

    /**
     * Ottieni totale predizioni
     */
    @Transactional(readOnly = true)
    public Long getTotalPredictions() {
        return modelRepository.getTotalPredictions();
    }

    /**
     * Ottieni dimensione totale storage
     */
    @Transactional(readOnly = true)
    public Long getTotalStorageSize() {
        return modelRepository.getTotalStorageSize();
    }

    /**
     * Ottieni modelli più utilizzati
     */
    @Transactional(readOnly = true)
    public Page<ModelEntity> getMostUsedModels(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return modelRepository.findMostUsed(pageable);
    }

    // ==== MANUTENZIONE ====

    /**
     * Trova modelli mai utilizzati
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findUnusedModels() {
        return modelRepository.findUnusedModels();
    }

    /**
     * Trova modelli non utilizzati da X giorni
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findStaleModels(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return modelRepository.findStaleModels(threshold);
    }

    /**
     * Lista bucket per storage type
     */
    @Transactional(readOnly = true)
    public List<String> getBucketsByStorageType(ModelEntity.StorageType storageType) {
        return modelRepository.findBucketsByStorageType(storageType);
    }

    /**
     * Recupera tutti i modelli con paginazione
     */
    @Transactional(readOnly = true)
    public Page<ModelEntity> getAllModels(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return modelRepository.findAll(pageable);
    }

    /**
     * Recupera tutti i modelli attivi dal database
     */
    @Transactional(readOnly = true)
    public List<ModelEntity> findActiveModels() {
        return modelRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Elimina un modello dal database
     */
    public boolean deleteModel(String modelName, String version) {
        log.info("🗑️ Eliminazione modello dal database: {} v{}", modelName, version);
        
        Optional<ModelEntity> modelOpt = modelRepository.findByModelNameAndVersion(modelName, version);
        if (modelOpt.isPresent()) {
            ModelEntity model = modelOpt.get();
            modelRepository.delete(model);
            log.info("✅ Modello {} v{} eliminato dal database (ID: {})", modelName, version, model.getId());
            return true;
        } else {
            log.warn("⚠️ Modello {} v{} non trovato nel database", modelName, version);
            return false;
        }
    }

    /**
     * Elimina un modello dal database per ID
     */
    public boolean deleteModelById(Long id) {
        log.info("🗑️ Eliminazione modello dal database per ID: {}", id);
        
        Optional<ModelEntity> modelOpt = modelRepository.findById(id);
        if (modelOpt.isPresent()) {
            ModelEntity model = modelOpt.get();
            modelRepository.delete(model);
            log.info("✅ Modello {} v{} eliminato dal database (ID: {})", 
                    model.getModelName(), model.getVersion(), id);
            return true;
        } else {
            log.warn("⚠️ Modello con ID {} non trovato nel database", id);
            return false;
        }
    }

    // ==== UTILITY METHODS ====

    /**
     * Crea un modello di esempio per test
     */
    public ModelEntity createSampleModel(String name, String version, ModelEntity.ModelType type) {
        return ModelEntity.builder()
                .modelName(name)
                .version(version)
                .type(type)
                .description("Modello di test creato da ModelDatabaseService")
                .filePath("/models/" + name + "-" + version + ".model")
                .fileSize(1024L * 1024L) // 1MB
                .fileHash("sha256:example-hash")
                .inputSchema("{\"type\": \"array\", \"items\": {\"type\": \"number\"}}")
                .outputSchema("{\"prediction\": \"number\", \"confidence\": \"number\"}")
                .metadata("{\"framework\": \"test\", \"created_by\": \"system\"}")
                .build();
    }

    /**
     * Crea un modello cloud di esempio
     */
    public ModelEntity createSampleCloudModel(String name, String version, 
                                              ModelEntity.StorageType storageType, 
                                              String bucketName, String region) {
        return ModelEntity.builder()
                .modelName(name)
                .version(version)
                .type(ModelEntity.ModelType.ONNX)
                .description("Modello cloud di test")
                .storageType(storageType)
                .filePath("models/" + name + "/" + version + ".onnx")
                .bucketName(bucketName)
                .bucketRegion(region)
                .storageClass("STANDARD")
                .fileSize(5L * 1024L * 1024L) // 5MB
                .fileHash("sha256:cloud-model-hash")
                .inputSchema("{\"features\": [\"number\"]}")
                .outputSchema("{\"class\": \"string\", \"probability\": \"number\"}")
                .metadata("{\"cloud\": true, \"region\": \"" + region + "\"}")
                .build();
    }
}
