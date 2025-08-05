package com.maiolix.maverick.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.service.ModelDatabaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller per testare l'integrazione con PostgreSQL
 */
@RestController
@RequestMapping("/api/v1/test/database")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Database Test", description = "Endpoint per testare l'integrazione PostgreSQL")
public class DatabaseTestController {

    private final ModelDatabaseService modelDatabaseService;

    @GetMapping("/status")
    @Operation(summary = "Verifica stato connessione database")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Test connessione base
            List<ModelEntity> models = modelDatabaseService.findAllActiveModels();
            
            status.put("connected", true);
            status.put("timestamp", LocalDateTime.now());
            status.put("totalModels", models.size());
            status.put("message", "Connessione PostgreSQL OK");
            
            // Statistiche aggiuntive
            try {
                Long totalPredictions = modelDatabaseService.getTotalPredictions();
                Long totalStorage = modelDatabaseService.getTotalStorageSize();
                
                status.put("totalPredictions", totalPredictions);
                status.put("totalStorageBytes", totalStorage);
                status.put("totalStorageMB", totalStorage / (1024.0 * 1024.0));
            } catch (Exception e) {
                status.put("statisticsError", e.getMessage());
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Errore connessione database: {}", e.getMessage());
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(status);
        }
    }

    @PostMapping("/create-sample")
    @Operation(summary = "Crea modello di esempio per test")
    public ResponseEntity<ModelEntity> createSampleModel() {
        try {
            // Crea un modello locale di esempio
            ModelEntity sample = modelDatabaseService.createSampleModel(
                    "database-test-model",
                    "1.0.0",
                    ModelEntity.ModelType.ONNX
            );
            
            ModelEntity saved = modelDatabaseService.saveModel(sample);
            log.info("Modello di test creato con ID: {}", saved.getId());
            
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("Errore creando modello di test: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create-cloud-sample")
    @Operation(summary = "Crea modello cloud di esempio per test")
    public ResponseEntity<ModelEntity> createCloudSampleModel() {
        try {
            // Crea un modello cloud di esempio
            ModelEntity cloudSample = modelDatabaseService.createSampleCloudModel(
                    "database-test-cloud-model",
                    "1.0.0",
                    ModelEntity.StorageType.S3,
                    "test-ml-bucket",
                    "eu-west-1"
            );
            
            ModelEntity saved = modelDatabaseService.saveModel(cloudSample);
            log.info("Modello cloud di test creato con ID: {}", saved.getId());
            
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("Errore creando modello cloud di test: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Ottieni statistiche database")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Conta modelli per tipo
            List<Object[]> countByType = modelDatabaseService.getModelCountByType();
            Map<String, Long> typeStats = new HashMap<>();
            for (Object[] row : countByType) {
                typeStats.put(row[0].toString(), ((Number) row[1]).longValue());
            }
            
            // Conta modelli per storage
            List<Object[]> countByStorage = modelDatabaseService.getModelCountByStorageType();
            Map<String, Long> storageStats = new HashMap<>();
            for (Object[] row : countByStorage) {
                storageStats.put(row[0].toString(), ((Number) row[1]).longValue());
            }
            
            stats.put("modelsByType", typeStats);
            stats.put("modelsByStorage", storageStats);
            stats.put("totalPredictions", modelDatabaseService.getTotalPredictions());
            stats.put("totalStorageSize", modelDatabaseService.getTotalStorageSize());
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Errore ottenendo statistiche: {}", e.getMessage());
            stats.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(stats);
        }
    }

    @PostMapping("/test-prediction")
    @Operation(summary = "Simula registrazione predizione")
    public ResponseEntity<Map<String, Object>> testPrediction(@RequestParam Long modelId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Registra una predizione
            modelDatabaseService.recordPrediction(modelId);
            
            // Recupera il modello aggiornato
            var model = modelDatabaseService.findById(modelId);
            
            if (model.isPresent()) {
                result.put("success", true);
                result.put("modelId", modelId);
                result.put("predictionCount", model.get().getPredictionCount());
                result.put("lastUsedAt", model.get().getLastUsedAt());
                result.put("message", "Predizione registrata con successo");
            } else {
                result.put("success", false);
                result.put("message", "Modello non trovato");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Errore registrando predizione: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/cloud-models")
    @Operation(summary = "Lista modelli cloud")
    public ResponseEntity<List<ModelEntity>> getCloudModels() {
        try {
            List<ModelEntity> cloudModels = modelDatabaseService.findCloudModels();
            return ResponseEntity.ok(cloudModels);
        } catch (Exception e) {
            log.error("Errore recuperando modelli cloud: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Pulisce i modelli di test")
    public ResponseEntity<Map<String, Object>> cleanup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Trova e disattiva i modelli di test
            var testModels = modelDatabaseService.searchByName("database-test", 0, 100);
            int cleaned = 0;
            
            for (ModelEntity model : testModels.getContent()) {
                if (model.getModelName().contains("database-test")) {
                    modelDatabaseService.deactivateModel(model.getId());
                    cleaned++;
                }
            }
            
            result.put("success", true);
            result.put("modelsDeactivated", cleaned);
            result.put("message", "Pulizia completata");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Errore durante pulizia: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
