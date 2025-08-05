package com.maiolix.maverick.controller;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.service.ModelDatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST per la gestione della repository di modelli ML
 */
@RestController
@RequestMapping("/api/v1/models/repository")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Model Repository", description = "Gestione repository di modelli ML con PostgreSQL")
public class ModelRepositoryController {

    private final ModelDatabaseService modelDatabaseService;

    // ==== OPERAZIONI CRUD ====

    @GetMapping
    @Operation(summary = "Lista tutti i modelli attivi", 
               description = "Recupera tutti i modelli attivi dal database PostgreSQL")
    public ResponseEntity<List<ModelEntity>> getAllModels() {
        List<ModelEntity> models = modelDatabaseService.findAllActiveModels();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera modello per ID", 
               description = "Trova un modello specifico tramite ID")
    public ResponseEntity<ModelEntity> getModelById(
            @Parameter(description = "ID del modello") @PathVariable Long id) {
        
        Optional<ModelEntity> model = modelDatabaseService.findById(id);
        return model.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}/version/{version}")
    @Operation(summary = "Recupera modello per nome e versione")
    public ResponseEntity<ModelEntity> getModelByNameAndVersion(
            @Parameter(description = "Nome del modello") @PathVariable String name,
            @Parameter(description = "Versione del modello") @PathVariable String version) {
        
        Optional<ModelEntity> model = modelDatabaseService.findByNameAndVersion(name, version);
        return model.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/uuid/{uuid}")
    @Operation(summary = "Recupera modello per UUID")
    public ResponseEntity<ModelEntity> getModelByUuid(
            @Parameter(description = "UUID del modello") @PathVariable String uuid) {
        
        Optional<ModelEntity> model = modelDatabaseService.findByUuid(uuid);
        return model.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Salva nuovo modello", 
               description = "Crea un nuovo modello nel database")
    public ResponseEntity<ModelEntity> saveModel(@Valid @RequestBody ModelEntity model) {
        try {
            ModelEntity saved = modelDatabaseService.saveModel(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Errore salvando modello: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deploy")
    @Operation(summary = "Deploy nuova versione modello", 
               description = "Deploya una nuova versione disattivando le precedenti")
    public ResponseEntity<ModelEntity> deployNewVersion(@Valid @RequestBody ModelEntity model) {
        try {
            ModelEntity deployed = modelDatabaseService.deployNewVersion(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(deployed);
        } catch (Exception e) {
            log.error("Errore nel deploy: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==== OPERAZIONI DI RICERCA ====

    @GetMapping("/search")
    @Operation(summary = "Ricerca modelli per nome")
    public ResponseEntity<Page<ModelEntity>> searchByName(
            @Parameter(description = "Nome da cercare") @RequestParam String name,
            @Parameter(description = "Numero pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina") @RequestParam(defaultValue = "10") int size) {
        
        Page<ModelEntity> results = modelDatabaseService.searchByName(name, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/fulltext")
    @Operation(summary = "Ricerca full-text nei modelli")
    public ResponseEntity<Page<ModelEntity>> fullTextSearch(
            @Parameter(description = "Termine di ricerca") @RequestParam String searchTerm,
            @Parameter(description = "Numero pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina") @RequestParam(defaultValue = "10") int size) {
        
        Page<ModelEntity> results = modelDatabaseService.fullTextSearch(searchTerm, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Trova modelli per tipo")
    public ResponseEntity<List<ModelEntity>> getModelsByType(
            @Parameter(description = "Tipo di modello") @PathVariable ModelEntity.ModelType type) {
        
        List<ModelEntity> models = modelDatabaseService.findByType(type);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/storage/{storageType}")
    @Operation(summary = "Trova modelli per tipo di storage")
    public ResponseEntity<List<ModelEntity>> getModelsByStorageType(
            @Parameter(description = "Tipo di storage") @PathVariable ModelEntity.StorageType storageType) {
        
        List<ModelEntity> models = modelDatabaseService.findByStorageType(storageType);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/cloud")
    @Operation(summary = "Lista modelli cloud")
    public ResponseEntity<List<ModelEntity>> getCloudModels() {
        List<ModelEntity> models = modelDatabaseService.findCloudModels();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/bucket/{bucketName}")
    @Operation(summary = "Trova modelli per bucket")
    public ResponseEntity<List<ModelEntity>> getModelsByBucket(
            @Parameter(description = "Nome del bucket") @PathVariable String bucketName) {
        
        List<ModelEntity> models = modelDatabaseService.findByBucket(bucketName);
        return ResponseEntity.ok(models);
    }

    // ==== OPERAZIONI DI AGGIORNAMENTO ====

    @PostMapping("/{id}/predict")
    @Operation(summary = "Registra predizione", 
               description = "Incrementa il contatore delle predizioni per un modello")
    public ResponseEntity<String> recordPrediction(
            @Parameter(description = "ID del modello") @PathVariable Long id) {
        
        try {
            modelDatabaseService.recordPrediction(id);
            return ResponseEntity.ok("Predizione registrata");
        } catch (Exception e) {
            log.error("Errore registrando predizione: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Errore: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Aggiorna stato modello")
    public ResponseEntity<String> updateStatus(
            @Parameter(description = "ID del modello") @PathVariable Long id,
            @Parameter(description = "Nuovo stato") @RequestParam ModelEntity.ModelStatus status) {
        
        boolean updated = modelDatabaseService.updateModelStatus(id, status);
        if (updated) {
            return ResponseEntity.ok("Stato aggiornato");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disattiva modello", 
               description = "Disattiva un modello (soft delete)")
    public ResponseEntity<String> deactivateModel(
            @Parameter(description = "ID del modello") @PathVariable Long id) {
        
        boolean deactivated = modelDatabaseService.deactivateModel(id);
        if (deactivated) {
            return ResponseEntity.ok("Modello disattivato");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==== STATISTICHE ====

    @GetMapping("/stats/count-by-type")
    @Operation(summary = "Conta modelli per tipo")
    public ResponseEntity<Map<String, Long>> getCountByType() {
        List<Object[]> counts = modelDatabaseService.getModelCountByType();
        Map<String, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue()
                ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/count-by-storage")
    @Operation(summary = "Conta modelli per storage type")
    public ResponseEntity<Map<String, Long>> getCountByStorageType() {
        List<Object[]> counts = modelDatabaseService.getModelCountByStorageType();
        Map<String, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue()
                ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/total-predictions")
    @Operation(summary = "Totale predizioni")
    public ResponseEntity<Long> getTotalPredictions() {
        Long total = modelDatabaseService.getTotalPredictions();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/total-storage")
    @Operation(summary = "Dimensione totale storage")
    public ResponseEntity<Long> getTotalStorageSize() {
        Long total = modelDatabaseService.getTotalStorageSize();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/most-used")
    @Operation(summary = "Modelli più utilizzati")
    public ResponseEntity<Page<ModelEntity>> getMostUsedModels(
            @Parameter(description = "Numero pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Dimensione pagina") @RequestParam(defaultValue = "10") int size) {
        
        Page<ModelEntity> models = modelDatabaseService.getMostUsedModels(page, size);
        return ResponseEntity.ok(models);
    }

    // ==== MANUTENZIONE ====

    @GetMapping("/maintenance/unused")
    @Operation(summary = "Trova modelli mai utilizzati")
    public ResponseEntity<List<ModelEntity>> getUnusedModels() {
        List<ModelEntity> models = modelDatabaseService.findUnusedModels();
        return ResponseEntity.ok(models);
    }

    @GetMapping("/maintenance/stale")
    @Operation(summary = "Trova modelli non utilizzati da X giorni")
    public ResponseEntity<List<ModelEntity>> getStaleModels(
            @Parameter(description = "Giorni di inattività") @RequestParam(defaultValue = "30") int days) {
        
        List<ModelEntity> models = modelDatabaseService.findStaleModels(days);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/buckets/{storageType}")
    @Operation(summary = "Lista bucket per storage type")
    public ResponseEntity<List<String>> getBucketsByStorageType(
            @Parameter(description = "Tipo di storage") @PathVariable ModelEntity.StorageType storageType) {
        
        List<String> buckets = modelDatabaseService.getBucketsByStorageType(storageType);
        return ResponseEntity.ok(buckets);
    }

    // ==== UTILITY ====

    @PostMapping("/test/sample-model")
    @Operation(summary = "Crea modello di test")
    public ResponseEntity<ModelEntity> createSampleModel(
            @Parameter(description = "Nome modello") @RequestParam String name,
            @Parameter(description = "Versione") @RequestParam String version,
            @Parameter(description = "Tipo modello") @RequestParam ModelEntity.ModelType type) {
        
        ModelEntity sample = modelDatabaseService.createSampleModel(name, version, type);
        ModelEntity saved = modelDatabaseService.saveModel(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/test/sample-cloud-model")
    @Operation(summary = "Crea modello cloud di test")
    public ResponseEntity<ModelEntity> createSampleCloudModel(
            @Parameter(description = "Nome modello") @RequestParam String name,
            @Parameter(description = "Versione") @RequestParam String version,
            @Parameter(description = "Storage type") @RequestParam ModelEntity.StorageType storageType,
            @Parameter(description = "Nome bucket") @RequestParam String bucketName,
            @Parameter(description = "Regione") @RequestParam String region) {
        
        ModelEntity sample = modelDatabaseService.createSampleCloudModel(name, version, storageType, bucketName, region);
        ModelEntity saved = modelDatabaseService.saveModel(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
