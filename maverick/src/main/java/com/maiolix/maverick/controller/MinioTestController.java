package com.maiolix.maverick.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.repository.MinioModelRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller per testare l'upload dei modelli su MinIO
 */
@RestController
@RequestMapping("/api/v1/minio/models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MinIO Models", description = "Gestione modelli ML su MinIO")
public class MinioTestController {

    private final MinioModelRepository minioRepository;

    /**
     * Upload di un modello
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload modello", description = "Carica un modello su MinIO con struttura modello/versione/")
    public ResponseEntity<Map<String, Object>> uploadModel(
            @Parameter(description = "File del modello") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome del modello") @RequestParam("modelName") String modelName,
            @Parameter(description = "Versione del modello") @RequestParam("version") String version) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📤 Richiesta upload: modello={}, versione={}, file={}", modelName, version, file.getOriginalFilename());
            
            // Validazione input
            if (file.isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "File vuoto");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (modelName == null || modelName.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "Nome modello richiesto");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (version == null || version.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "Versione richiesta");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Upload del modello
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            minioRepository.uploadModel(modelName, version, fileName, 
                                      file.getInputStream(), file.getSize(), contentType);
            
            // Risposta di successo
            response.put("status", "SUCCESS");
            response.put("message", "Modello caricato con successo");
            response.put("modelName", modelName);
            response.put("version", version);
            response.put("fileName", fileName);
            response.put("path", modelName + "/" + version + "/" + fileName);
            response.put("size", file.getSize());
            response.put("bucket", minioRepository.getDefaultBucket());
            
            log.info("✅ Upload completato: {}/{}/{}", modelName, version, fileName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Errore upload: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "Upload fallito: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test connessione MinIO
     */
    @GetMapping("/test")
    @Operation(summary = "Test connessione", description = "Verifica la connessione a MinIO")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            minioRepository.testConnection();
            
            response.put("status", "SUCCESS");
            response.put("message", "Connessione MinIO OK");
            response.put("endpoint", minioRepository.getEndpoint());
            response.put("bucket", minioRepository.getDefaultBucket());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Test connessione fallito: {}", e.getMessage());
            
            response.put("status", "ERROR");
            response.put("message", "Test fallito: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}