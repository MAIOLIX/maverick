package com.maiolix.maverick.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Esempio di integrazione del sistema di autenticazione JWT con il MaverickController esistente.
 * 
 * Questo file mostra come aggiungere le annotazioni @PreAuthorize ai metodi del controller
 * per implementare il controllo degli accessi basato sui ruoli.
 * 
 * IMPORTANTE: Questo è solo un ESEMPIO. Per implementare l'autenticazione nel controller esistente,
 * aggiungere le annotazioni @PreAuthorize mostrate qui ai metodi corrispondenti in MaverickController.java
 */
public class MaverickControllerSecurityExample {

    // ================================================================================
    // METODI CHE RICHIEDONO RUOLO ADMIN
    // ================================================================================

    /**
     * Upload modello - Solo ADMIN
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelName") String modelName,
            @RequestParam("version") String version,
            @RequestParam("type") String modelType,
            @RequestParam(value = "description", required = false) String description) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Caricamento modello in memoria - Solo ADMIN
     */
    @PostMapping("/load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> loadModel(
            @RequestParam("modelName") String modelName,
            @RequestParam("version") String version) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Rimozione modello dalla memoria - Solo ADMIN
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeModel(
            @RequestParam("modelName") String modelName,
            @RequestParam("version") String version) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Cancellazione definitiva modello - Solo ADMIN
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteModel(
            @RequestParam("modelName") String modelName,
            @RequestParam("version") String version,
            @RequestParam(value = "deleteFromStorage", defaultValue = "false") boolean deleteFromStorage) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Bootstrap reload - Solo ADMIN
     */
    @PostMapping("/bootstrap/reload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reloadBootstrap() {
        // Implementazione esistente...
        return null;
    }

    /**
     * Modelli in memoria (informazioni dettagliate) - Solo ADMIN
     */
    @GetMapping("/models-in-memory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getModelsInMemory() {
        // Implementazione esistente...
        return null;
    }

    /**
     * Modelli nel database (gestione) - Solo ADMIN
     */
    @GetMapping("/models-database")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getModelsFromDatabase(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadTimestamp") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Audit bootstrap - Solo ADMIN
     */
    @GetMapping("/bootstrap/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBootstrapAudit() {
        // Implementazione esistente...
        return null;
    }

    /**
     * Tutti gli schema dei modelli - Solo ADMIN
     */
    @GetMapping("/models/schemas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllModelSchemas() {
        // Implementazione esistente...
        return null;
    }

    // ================================================================================
    // METODI ACCESSIBILI SIA AD ADMIN CHE A PREDICTOR
    // ================================================================================

    /**
     * Predizione - ADMIN e PREDICTOR
     */
    @PostMapping("/predict/{version}/{modelName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PREDICTOR')")
    public ResponseEntity<Map<String, Object>> predict(
            @PathVariable String version,
            @PathVariable String modelName,
            @RequestBody Map<String, Object> inputData) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Schema input del modello - ADMIN e PREDICTOR
     */
    @GetMapping("/models/{modelName}/versions/{version}/input-schema")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PREDICTOR')")
    public ResponseEntity<Map<String, Object>> getModelInputSchema(
            @PathVariable String modelName,
            @PathVariable String version) {
        // Implementazione esistente...
        return null;
    }

    /**
     * Informazioni del modello - ADMIN e PREDICTOR
     */
    @GetMapping("/models/{modelName}/versions/{version}/info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PREDICTOR')")
    public ResponseEntity<Map<String, Object>> getModelInfo(
            @PathVariable String modelName,
            @PathVariable String version) {
        // Implementazione esistente...
        return null;
    }

    // ================================================================================
    // CONFIGURAZIONE SECURITY NEL MAVERICK CONTROLLER ESISTENTE
    // ================================================================================

    /*
     * PASSI PER INTEGRARE L'AUTENTICAZIONE NEL CONTROLLER ESISTENTE:
     * 
     * 1. Aggiungere import:
     *    import org.springframework.security.access.prepost.PreAuthorize;
     * 
     * 2. Aggiungere le annotazioni @PreAuthorize come mostrato sopra a ogni metodo
     * 
     * 3. Aggiornare la configurazione SecurityConfig per mappare gli endpoint:
     *    - Da /api/v1/maverick/* a /api/models/*
     *    - Oppure aggiornare SecurityConfig per usare /api/v1/maverick/*
     * 
     * 4. Test degli endpoint con i token JWT generati dal sistema di autenticazione
     * 
     * NOTA: Per compatibilità con i client esistenti, si consiglia di mantenere
     * gli endpoint attuali e aggiungere gradualmente le nuove API protette.
     */

    /*
     * MAPPATURA SICUREZZA CONSIGLIATA:
     * 
     * SOLO ADMIN:
     * - POST /api/v1/maverick/upload
     * - POST /api/v1/maverick/load  
     * - DELETE /api/v1/maverick/remove
     * - DELETE /api/v1/maverick/delete
     * - POST /api/v1/maverick/bootstrap/reload
     * - GET /api/v1/maverick/models-in-memory
     * - GET /api/v1/maverick/models-database
     * - GET /api/v1/maverick/bootstrap/audit
     * - GET /api/v1/maverick/models/schemas
     * 
     * ADMIN + PREDICTOR:
     * - POST /api/v1/maverick/predict/{version}/{modelName}
     * - GET /api/v1/maverick/models/{modelName}/versions/{version}/input-schema
     * - GET /api/v1/maverick/models/{modelName}/versions/{version}/info
     */
}
