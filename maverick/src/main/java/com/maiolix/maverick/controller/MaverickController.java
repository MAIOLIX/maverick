package com.maiolix.maverick.controller;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.constants.MaverickConstants;
import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.registry.ModelRegistry;
import com.maiolix.maverick.repository.MinioModelRepository;
import com.maiolix.maverick.service.IModelService;
import com.maiolix.maverick.service.ModelBootstrapService;
import com.maiolix.maverick.service.ModelDatabaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller principale Maverick per gestione modelli ML
 * Integra database PostgreSQL e storage MinIO
 */
@RestController
@RequestMapping("/api/v1/maverick")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maverick", description = "API principale per gestione modelli ML")
public class MaverickController {

    private final ModelDatabaseService modelDatabaseService;
    private final MinioModelRepository minioRepository;
    private final IModelService modelService;
    private final ModelBootstrapService modelBootstrapService;

    /**
     * Upload completo: carica modello su MinIO e salva metadati nel database
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload modello completo", 
               description = "Carica un modello su MinIO e salva i metadati nel database PostgreSQL")
    public ResponseEntity<Map<String, Object>> uploadModel(
            @Parameter(description = "File del modello") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome del modello") @RequestParam("modelName") String modelName,
            @Parameter(description = "Versione del modello") @RequestParam("version") String version,
            @Parameter(description = "Tipo di modello") @RequestParam("type") String modelType,
            @Parameter(description = "Descrizione (opzionale)") @RequestParam(value = "description", required = false) String description) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🚀 Upload modello: {} v{} tipo={}", modelName, version, modelType);
            
            // === VALIDAZIONE INPUT ===
            validateUploadInput(file, modelName, version, modelType);
            
            // === VERIFICA DUPLICATI ===
            if (modelDatabaseService.findByNameAndVersion(modelName, version).isPresent()) {
                throw new ModelUploadException(MaverickConstants.MODELLO + modelName + MaverickConstants.VERSIONE + version + " già esistente");
            }
            
            // === DETERMINA TIPO MODELLO ===
            ModelEntity.ModelType type = parseModelType(modelType);
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            // === UPLOAD SU MINIO ===
            log.info("📤 Caricamento su MinIO: {}/{}/{}", modelName, version, fileName);
            minioRepository.uploadModel(modelName, version, fileName, 
                                      file.getInputStream(), file.getSize(), contentType);
            
            // === CALCOLO HASH PER INTEGRITÀ ===
            String fileHash = calculateFileHash(file);
            
            // === CREAZIONE ENTITY ===
            ModelEntity modelEntity = ModelEntity.builder()
                    .modelName(modelName)
                    .version(version)
                    .type(type)
                    .description(description)
                    .storageType(ModelEntity.StorageType.MINIO)
                    .filePath(modelName + "/" + version + "/" + fileName)
                    .bucketName(minioRepository.getDefaultBucket())
                    .fileSize(file.getSize())
                    .fileHash(fileHash)
                    .contentType(contentType)
                    .status(ModelEntity.ModelStatus.READY)
                    .isActive(false) // Modello non attivo di default
                    .createdBy("api-user")
                    .updatedBy("api-user")
                    .build();
            
            // === SALVATAGGIO DATABASE ===
            log.info("💾 Salvataggio nel database...");
            ModelEntity savedModel = modelDatabaseService.saveModel(modelEntity);
            
            // === RISPOSTA DI SUCCESSO ===
            response.put(MaverickConstants.STATUS, MaverickConstants.SUCCESS);
            response.put(MaverickConstants.MESSAGE, "Modello caricato con successo (non attivo)");
            response.put("modelId", savedModel.getId());
            response.put("modelUuid", savedModel.getModelUuid());
            response.put(MaverickConstants.MODEL_NAME, modelName);
            response.put(MaverickConstants.VERSION, version);
            response.put("type", type.toString());
            response.put("fileName", fileName);
            response.put("minioPath", modelName + "/" + version + "/" + fileName);
            response.put("bucket", minioRepository.getDefaultBucket());
            response.put(MaverickConstants.FILE_SIZE, file.getSize());
            response.put("fileHash", fileHash);
            response.put(MaverickConstants.IS_ACTIVE, false);
            response.put("createdAt", savedModel.getCreatedAt());
            response.put("note", "Usa /load per attivare il modello e caricarlo in memoria");
            
            log.info("✅ Upload completato: ID={}, UUID={}", savedModel.getId(), savedModel.getModelUuid());
            
            return ResponseEntity.ok(response);
            
        } catch (ModelUploadException e) {
            log.error("❌ Errore validazione upload: {}", e.getMessage());
            throw e; // GlobalExceptionHandler gestirà la risposta
            
        } catch (Exception e) {
            log.error("❌ Errore upload modello: {}", e.getMessage(), e);
            throw new ModelUploadException("Upload fallito: " + e.getMessage(), e);
        }
    }

    /**
     * Validazione input per upload
     */
    private void validateUploadInput(MultipartFile file, String modelName, String version, String modelType) {
        if (file == null || file.isEmpty()) {
            throw new ModelUploadException("File richiesto");
        }
        
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelUploadException("Nome modello richiesto");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new ModelUploadException("Versione richiesta");
        }
        
        if (modelType == null || modelType.trim().isEmpty()) {
            throw new ModelUploadException("Tipo modello richiesto");
        }
        
        // Validazione formato nome modello (solo lettere, numeri, underscore, trattini)
        if (!modelName.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ModelUploadException("Nome modello deve contenere solo lettere, numeri, underscore e trattini");
        }
        
        // Validazione formato versione
        if (!version.matches("^v?\\d+\\.\\d+(\\.\\d+)?$")) {
            throw new ModelUploadException("Versione deve essere nel formato v1.0 o 1.0.0");
        }
        
        // Validazione dimensione file (max 100MB)
        long maxSize = 100L * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new ModelUploadException("File troppo grande. Massimo 100MB consentiti");
        }
    }

    /**
     * Parsing del tipo di modello
     */
    private ModelEntity.ModelType parseModelType(String modelType) {
        try {
            return ModelEntity.ModelType.valueOf(modelType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ModelUploadException("Tipo modello non valido: " + modelType + 
                ". Valori supportati: ONNX, PMML, MOJO, H2O, ONNX_EXT");
        }
    }

    /**
     * Calcolo hash per integrità del file (semplificato)
     */
    private String calculateFileHash(MultipartFile file) {
        try {
            // Per ora usiamo una combinazione di nome file + dimensione + timestamp
            // In futuro si può implementare SHA-256 vero
            String hashInput = file.getOriginalFilename() + "_" + file.getSize() + "_" + System.currentTimeMillis();
            return String.valueOf(hashInput.hashCode());
        } catch (Exception e) {
            log.warn("Impossibile calcolare hash file: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Carica un modello in memoria dalla storage
     */
    @PostMapping("/load")
    @Operation(summary = "Carica modello in memoria", 
               description = "Carica un modello dal database/MinIO nella cache in memoria per predizioni")
    public ResponseEntity<Map<String, Object>> loadModel(
            @Parameter(description = "Nome del modello") @RequestParam("modelName") String modelName,
            @Parameter(description = "Versione del modello") @RequestParam("version") String version) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔄 Caricamento modello in memoria: {} v{}", modelName, version);
            
            // === VERIFICA ESISTENZA NEL DATABASE ===
            ModelEntity modelEntity = modelDatabaseService.findByNameAndVersion(modelName, version)
                .orElseThrow(() -> new ModelNotFoundException(MaverickConstants.MODELLO + modelName + MaverickConstants.VERSIONE + version + MaverickConstants.NON_TROVATO_DB));
            
            // === VERIFICA SE GIÀ IN MEMORIA ===
            String modelKey = modelName + "_" + version;
            if (ModelRegistry.exists(modelName, version)) {
                log.info("⚡ Modello {} già presente in memoria", modelKey);
                response.put(MaverickConstants.STATUS, MaverickConstants.SUCCESS);
                response.put(MaverickConstants.MESSAGE, "Modello già caricato in memoria");
                response.put(MaverickConstants.MODEL_NAME, modelName);
                response.put(MaverickConstants.VERSION, version);
                response.put("cached", true);
                return ResponseEntity.ok(response);
            }
            
            // === CARICAMENTO REALE IN MEMORIA ===
            log.info("📥 Download modello da MinIO: {}", modelEntity.getFilePath());
            
            loadModelIntoMemoryCache(modelName, version, modelEntity);
            
            // === AGGIORNAMENTO TIMESTAMP E ATTIVAZIONE ===
            modelEntity.setLastUsedAt(LocalDateTime.now());
            modelEntity.setIsActive(true); // Attiva il modello quando caricato in memoria
            modelDatabaseService.saveModel(modelEntity); // Salva le modifiche nel database
            
            log.info("✅ Modello {} v{} attivato nel database", modelName, version);
            
            response.put(MaverickConstants.STATUS, MaverickConstants.SUCCESS);
            response.put(MaverickConstants.MESSAGE, "Modello caricato e attivato con successo in memoria");
            response.put(MaverickConstants.MODEL_NAME, modelName);
            response.put(MaverickConstants.VERSION, version);
            response.put("type", modelEntity.getType().toString());
            response.put(MaverickConstants.FILE_SIZE, modelEntity.getFileSize());
            response.put("minioPath", modelEntity.getFilePath());
            response.put("loadedAt", System.currentTimeMillis());
            response.put("cached", true);
            response.put(MaverickConstants.IS_ACTIVE, true);
            
            log.info("✅ Modello {} v{} caricato in memoria", modelName, version);
            
            return ResponseEntity.ok(response);
            
        } catch (ModelNotFoundException e) {
            log.error("❌ Modello non trovato: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("❌ Errore caricamento modello: {}", e.getMessage(), e);
            throw new ModelUploadException("Caricamento modello fallito: " + e.getMessage(), e);
        }
    }

    /**
     * Rimuove un modello dalla cache in memoria e lo disattiva nel database
     */
    @DeleteMapping("/remove")
    @Operation(summary = "Rimuovi modello dalla memoria e disattiva", 
               description = "Rimuove un modello dalla cache in memoria e imposta isActive=false nel database")
    public ResponseEntity<Map<String, Object>> removeModel(
            @Parameter(description = "Nome del modello") @RequestParam("modelName") String modelName,
            @Parameter(description = "Versione del modello") @RequestParam("version") String version) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🗑️ Rimozione modello dalla memoria e disattivazione: {} v{}", modelName, version);
            
            // === VERIFICA ESISTENZA NEL DATABASE ===
            ModelEntity modelEntity = modelDatabaseService.findByNameAndVersion(modelName, version)
                .orElseThrow(() -> new ModelNotFoundException(MaverickConstants.MODELLO + modelName + MaverickConstants.VERSIONE + version + MaverickConstants.NON_TROVATO_DB));
            
            // === RIMOZIONE DALLA CACHE ===
            boolean removed = modelService.removeModel(modelName, version);
            
            // === DISATTIVAZIONE NEL DATABASE ===
            modelEntity.setIsActive(false);
            modelDatabaseService.saveModel(modelEntity);
            
            if (removed) {
                response.put("status", "SUCCESS");
                response.put("message", "Modello rimosso dalla memoria e disattivato con successo");
                response.put("modelName", modelName);
                response.put("version", version);
                response.put("removedAt", System.currentTimeMillis());
                response.put("isActive", false);
                
                log.info("✅ Modello {} v{} rimosso dalla memoria e disattivato nel database", modelName, version);
            } else {
                response.put("status", "SUCCESS");
                response.put("message", "Modello non era in memoria ma è stato disattivato nel database");
                response.put("modelName", modelName);
                response.put("version", version);
                response.put("removedAt", System.currentTimeMillis());
                response.put("isActive", false);
                
                log.warn("⚠️ Modello {} v{} non era in memoria, ma disattivato nel database", modelName, version);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore rimozione modello: {}", e.getMessage(), e);
            throw new ModelUploadException("Rimozione modello fallita: " + e.getMessage(), e);
        }
    }

    /**
     * Esegue predizione usando un modello caricato in memoria
     */
    @PostMapping("/predict/{version}/{modelName}")
    @Operation(summary = "Predizione modello", 
               description = "Esegue una predizione usando un modello caricato in memoria")
    public ResponseEntity<Map<String, Object>> predict(
            @Parameter(description = "Versione del modello") @PathVariable String version,
            @Parameter(description = "Nome del modello") @PathVariable String modelName,
            @Parameter(description = "Dati input per la predizione") @RequestBody Object inputData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🎯 Predizione: {} v{}", modelName, version);
            
            // === REGISTRAZIONE UTILIZZO ===
            ModelEntity modelEntity = modelDatabaseService.findByNameAndVersion(modelName, version)
                .orElseThrow(() -> new ModelNotFoundException("Modello " + modelName + " versione " + version + " non trovato"));
            
            // === ESECUZIONE PREDIZIONE ===
            long startTime = System.currentTimeMillis();
            Object prediction = modelService.predict(modelName, version, inputData);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // === AGGIORNAMENTO STATISTICHE ===
            modelDatabaseService.recordPrediction(modelEntity.getId());
            
            // === RISPOSTA ===
            response.put("status", "SUCCESS");
            response.put("prediction", prediction);
            response.put("modelName", modelName);
            response.put("version", version);
            response.put("executionTimeMs", executionTime);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ Predizione completata in {}ms: {} v{}", executionTime, modelName, version);
            
            return ResponseEntity.ok(response);
            
        } catch (ModelNotFoundException e) {
            log.error("❌ Modello non trovato per predizione: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("❌ Errore durante predizione: {}", e.getMessage(), e);
            throw new ModelPredictionException("Predizione fallita: " + e.getMessage(), e);
        }
    }

    /**
     * Lista tutti i modelli attualmente caricati in memoria
     */
    @GetMapping("/models-in-memory")
    @Operation(summary = "Lista modelli in memoria", 
               description = "Restituisce la lista di tutti i modelli attualmente caricati nella cache in memoria")
    public ResponseEntity<Map<String, Object>> getModelsInMemory() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📋 Richiesta lista modelli in memoria");
            
            // === RECUPERA MODELLI DALLA CACHE ===
            var cachedModels = ModelRegistry.getAllModels();
            
            // === COSTRUZIONE LISTA DETTAGLIATA ===
            List<Map<String, Object>> modelsList = cachedModels.stream()
                .map(entry -> {
                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put(MaverickConstants.MODEL_NAME, entry.getModelName());
                    modelInfo.put(MaverickConstants.VERSION, entry.getVersion());
                    modelInfo.put("type", entry.getType());
                    modelInfo.put("key", entry.getKey());
                    modelInfo.put("hasHandler", entry.getHandler() != null);
                    modelInfo.put("hasLabelMapping", entry.getLabelMapping() != null);
                    return modelInfo;
                })
                .toList();
            
            // === STATISTICHE GENERALI ===
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalModels", modelsList.size());
            statistics.put("modelTypes", modelsList.stream()
                .map(model -> (String) model.get("type"))
                .distinct()
                .toList());
            statistics.put("uniqueModelNames", modelsList.stream()
                .map(model -> (String) model.get(MaverickConstants.MODEL_NAME))
                .distinct()
                .count());
            
            // === RISPOSTA ===
            response.put(MaverickConstants.STATUS, MaverickConstants.SUCCESS);
            response.put(MaverickConstants.MESSAGE, "Lista modelli in memoria recuperata con successo");
            response.put("models", modelsList);
            response.put(MaverickConstants.STATISTICS, statistics);
            response.put(MaverickConstants.TIMESTAMP, System.currentTimeMillis());
            
            log.info("✅ Lista modelli in memoria: {} modelli trovati", modelsList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore recupero lista modelli: {}", e.getMessage(), e);
            
            response.put(MaverickConstants.STATUS, MaverickConstants.ERROR);
            response.put(MaverickConstants.MESSAGE, "Errore durante recupero lista modelli: " + e.getMessage());
            response.put(MaverickConstants.TIMESTAMP, System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Elimina completamente un modello da memoria, database e MinIO
     */
    @DeleteMapping("/delete")
    @Operation(summary = "Elimina modello completamente", 
               description = "Elimina un modello da memoria, database e storage MinIO")
    public ResponseEntity<Map<String, Object>> deleteModelCompletely(
            @Parameter(description = "Nome del modello") @RequestParam("modelName") String modelName,
            @Parameter(description = "Versione del modello") @RequestParam("version") String version) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🗑️ Eliminazione completa modello: {} v{}", modelName, version);
            
            // === VERIFICA ESISTENZA NEL DATABASE ===
            ModelEntity modelEntity = modelDatabaseService.findByNameAndVersion(modelName, version)
                .orElseThrow(() -> new ModelNotFoundException(MaverickConstants.MODELLO + modelName + MaverickConstants.VERSIONE + version + MaverickConstants.NON_TROVATO_DB));
            
            boolean memoryRemoved = false;
            boolean minioDeleted = false;
            boolean dbDeleted = false;
            
            // === 1. RIMOZIONE DALLA MEMORIA ===
            try {
                memoryRemoved = modelService.removeModel(modelName, version);
                log.info("🧠 Memoria: {}", memoryRemoved ? "rimosso" : "non era presente");
            } catch (Exception e) {
                log.warn("⚠️ Errore rimozione dalla memoria: {}", e.getMessage());
            }
            
            // === 2. ELIMINAZIONE DA MINIO ===
            try {
                minioDeleted = minioRepository.deleteModel(modelEntity.getFilePath());
                log.info("📦 MinIO: {}", minioDeleted ? "eliminato" : "errore eliminazione");
            } catch (Exception e) {
                log.warn("⚠️ Errore eliminazione da MinIO: {}", e.getMessage());
            }
            
            // === 3. ELIMINAZIONE DAL DATABASE ===
            try {
                dbDeleted = modelDatabaseService.deleteModel(modelName, version);
                log.info("💾 Database: {}", dbDeleted ? "eliminato" : "errore eliminazione");
            } catch (Exception e) {
                log.warn("⚠️ Errore eliminazione dal database: {}", e.getMessage());
            }
            
            // === RISPOSTA DETTAGLIATA ===
            response.put("status", "SUCCESS");
            response.put("message", "Eliminazione modello completata");
            response.put("modelName", modelName);
            response.put("version", version);
            response.put("deletedAt", System.currentTimeMillis());
            response.put("operations", Map.of(
                "memoryRemoved", memoryRemoved,
                "minioDeleted", minioDeleted,
                "databaseDeleted", dbDeleted
            ));
            
            log.info("✅ Eliminazione completa {} v{} - Memoria:{} MinIO:{} DB:{}", 
                    modelName, version, memoryRemoved, minioDeleted, dbDeleted);
            
            return ResponseEntity.ok(response);
            
        } catch (ModelNotFoundException e) {
            log.error("❌ Modello non trovato: {}", e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("❌ Errore eliminazione completa modello: {}", e.getMessage(), e);
            throw new ModelUploadException("Eliminazione modello fallita: " + e.getMessage(), e);
        }
    }

    /**
     * Lista tutti i modelli salvati nel database
     */
    @GetMapping("/models-database")
    @Operation(summary = "Lista modelli nel database", 
               description = "Restituisce la lista completa di tutti i modelli salvati nel database")
    public ResponseEntity<Map<String, Object>> getModelsInDatabase(
            @Parameter(description = "Numero pagina (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina") @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📋 Richiesta lista modelli nel database - Pagina: {}, Dimensione: {}", page, size);
            
            // === RECUPERA MODELLI DAL DATABASE ===
            var modelsPage = modelDatabaseService.getAllModels(page, size);
            
            // === COSTRUZIONE LISTA DETTAGLIATA ===
            List<Map<String, Object>> modelsList = modelsPage.getContent().stream()
                .map(model -> {
                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put("id", model.getId());
                    modelInfo.put("modelUuid", model.getModelUuid());
                    modelInfo.put("modelName", model.getModelName());
                    modelInfo.put("version", model.getVersion());
                    modelInfo.put("type", model.getType().toString());
                    modelInfo.put("description", model.getDescription());
                    modelInfo.put("filePath", model.getFilePath());
                    modelInfo.put("fileSize", model.getFileSize());
                    modelInfo.put("fileHash", model.getFileHash());
                    modelInfo.put("storageType", model.getStorageType().toString());
                    modelInfo.put("bucketName", model.getBucketName());
                    modelInfo.put("isActive", model.getIsActive());
                    modelInfo.put("status", model.getStatus().toString());
                    modelInfo.put("predictionCount", model.getPredictionCount());
                    modelInfo.put("lastUsedAt", model.getLastUsedAt());
                    modelInfo.put("createdAt", model.getCreatedAt());
                    modelInfo.put("updatedAt", model.getUpdatedAt());
                    return modelInfo;
                })
                .toList();
            
            // === STATISTICHE GENERALI ===
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalModels", modelsPage.getTotalElements());
            statistics.put("totalPages", modelsPage.getTotalPages());
            statistics.put("currentPage", page);
            statistics.put("pageSize", size);
            statistics.put("modelTypes", modelsList.stream()
                .map(model -> (String) model.get("type"))
                .distinct()
                .toList());
            statistics.put("activeModels", modelsList.stream()
                .mapToLong(model -> Boolean.TRUE.equals(model.get(MaverickConstants.IS_ACTIVE)) ? 1L : 0L)
                .sum());
            statistics.put("storageTypes", modelsList.stream()
                .map(model -> (String) model.get("storageType"))
                .distinct()
                .toList());
            
            // === RISPOSTA ===
            response.put("status", "SUCCESS");
            response.put("message", "Lista modelli nel database recuperata con successo");
            response.put("models", modelsList);
            response.put("pagination", Map.of(
                "totalElements", modelsPage.getTotalElements(),
                "totalPages", modelsPage.getTotalPages(),
                "currentPage", page,
                "pageSize", size,
                "hasNext", modelsPage.hasNext(),
                "hasPrevious", modelsPage.hasPrevious()
            ));
            response.put("statistics", statistics);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ Lista modelli nel database: {} modelli trovati (pagina {}/{})", 
                    modelsList.size(), page + 1, modelsPage.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore recupero lista modelli dal database: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "Errore durante recupero lista modelli dal database: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Ricarica manualmente tutti i modelli attivi dal database
     */
    @PostMapping("/bootstrap/reload")
    @Operation(summary = "Ricarica modelli attivi", 
               description = "Ricarica manualmente tutti i modelli attivi dal database nella cache in memoria")
    public ResponseEntity<Map<String, Object>> reloadActiveModels() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔄 Richiesta ricaricamento manuale modelli attivi");
            
            // === AUDIT PRIMA DEL RICARICAMENTO ===
            var activeModels = modelDatabaseService.findActiveModels();
            var cachedModels = ModelRegistry.getAllModels();
            int dbActiveCount = activeModels.size();
            int cacheCount = cachedModels.size();
            
            // === RICARICAMENTO ===
            modelBootstrapService.reloadAllActiveModels();
            
            // === AUDIT DOPO IL RICARICAMENTO ===
            var newCachedModels = ModelRegistry.getAllModels();
            int newCacheCount = newCachedModels.size();
            
            // === RISPOSTA ===
            response.put("status", "SUCCESS");
            response.put("message", "Ricaricamento modelli completato");
            response.put("before", Map.of(
                "databaseActive", dbActiveCount,
                "memoryCache", cacheCount
            ));
            response.put("after", Map.of(
                "memoryCache", newCacheCount
            ));
            response.put("reloadedAt", System.currentTimeMillis());
            
            log.info("✅ Ricaricamento completato - DB: {}, Cache prima: {}, Cache dopo: {}", 
                    dbActiveCount, cacheCount, newCacheCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore ricaricamento modelli: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "Errore durante ricaricamento modelli: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Audit dello stato modelli (database vs memoria)
     */
    @GetMapping("/bootstrap/audit")
    @Operation(summary = "Audit stato modelli", 
               description = "Verifica coerenza tra modelli attivi nel database e modelli in memoria")
    public ResponseEntity<Map<String, Object>> auditModelsStatus() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📊 Richiesta audit stato modelli");
            
            // === RECUPERA DATI ===
            var activeModels = modelDatabaseService.findActiveModels();
            var cachedModels = ModelRegistry.getAllModels();
            
            // === ANALISI COERENZA ===
            List<String> dbKeys = activeModels.stream()
                .map(model -> model.getModelName() + "_" + model.getVersion())
                .toList();
                
            List<String> cacheKeys = cachedModels.stream()
                .map(com.maiolix.maverick.registry.ModelCacheEntry::getKey)
                .toList();
            
            // Modelli attivi nel DB ma non in cache
            List<String> missingInCache = dbKeys.stream()
                .filter(key -> !cacheKeys.contains(key))
                .toList();
                
            // Modelli in cache ma non attivi nel DB
            List<String> extraInCache = cacheKeys.stream()
                .filter(key -> !dbKeys.contains(key))
                .toList();
            
            boolean isConsistent = missingInCache.isEmpty() && extraInCache.isEmpty();
            
            // === STATISTICHE DETTAGLIATE ===
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("databaseActiveModels", activeModels.size());
            statistics.put("memoryCachedModels", cachedModels.size());
            statistics.put("isConsistent", isConsistent);
            statistics.put("missingInCache", missingInCache.size());
            statistics.put("extraInCache", extraInCache.size());
            
            // === RISPOSTA ===
            response.put("status", "SUCCESS");
            response.put("message", "Audit modelli completato");
            response.put("statistics", statistics);
            response.put("details", Map.of(
                "activeModelsInDb", dbKeys,
                "cachedModelsInMemory", cacheKeys,
                "missingInCache", missingInCache,
                "extraInCache", extraInCache
            ));
            response.put("timestamp", System.currentTimeMillis());
            
            if (isConsistent) {
                log.info("✅ Audit completato - Coerenza DB-Memoria verificata");
            } else {
                log.warn("⚠️ Audit completato - Inconsistenze rilevate: {} mancanti, {} extra", 
                        missingInCache.size(), extraInCache.size());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore audit modelli: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "Errore durante audit modelli: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // === METODI HELPER PRIVATI ===

    /**
     * Carica un modello nella cache in memoria
     */
    private void loadModelIntoMemoryCache(String modelName, String version, ModelEntity modelEntity) {
        try {
            // Download del file da MinIO
            InputStream modelStream = minioRepository.downloadModel(modelEntity.getFilePath());
            
            // Crea l'handler per il modello
            Object handler = modelService.createModelHandler(modelStream, modelEntity.getType().toString());
            
            // Registra il modello con l'handler nella cache
            ModelRegistry.register(modelName, modelEntity.getType().toString(), version, (IModelHandler) handler);
            
            log.info("✅ Handler creato e registrato per modello {} v{}", modelName, version);
            
        } catch (Exception e) {
            log.error("❌ Errore creazione handler: {}", e.getMessage(), e);
            throw new ModelUploadException("Impossibile creare handler per il modello: " + e.getMessage(), e);
        }
    }
}
