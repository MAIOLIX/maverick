package com.maiolix.maverick.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.maiolix.maverick.entity.ModelEntity;
import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.registry.ModelRegistry;
import com.maiolix.maverick.repository.MinioModelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service per il caricamento automatico dei modelli attivi all'avvio dell'applicazione
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelBootstrapService {

    private final ModelDatabaseService modelDatabaseService;
    private final MinioModelRepository minioRepository;
    private final IModelService modelService;

    /**
     * Carica automaticamente tutti i modelli attivi all'avvio dell'applicazione
     * Viene eseguito dopo che l'applicazione è completamente inizializzata
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadActiveModelsOnStartup() {
        log.info("🚀 Avvio caricamento automatico modelli attivi...");
        
        try {
            // === RECUPERA MODELLI ATTIVI DAL DATABASE ===
            List<ModelEntity> activeModels = modelDatabaseService.findActiveModels();
            
            if (activeModels.isEmpty()) {
                log.info("📋 Nessun modello attivo trovato nel database");
                return;
            }
            
            log.info("📋 Trovati {} modelli attivi da caricare", activeModels.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // === CARICAMENTO MODELLI IN MEMORIA ===
            for (ModelEntity model : activeModels) {
                try {
                    loadModelIntoMemory(model);
                    successCount++;
                    log.info("✅ Modello caricato: {} v{}", model.getModelName(), model.getVersion());
                    
                } catch (Exception e) {
                    failureCount++;
                    log.error("❌ Errore caricamento modello {} v{}: {}", 
                            model.getModelName(), model.getVersion(), e.getMessage(), e);
                    
                    // Disattiva il modello se non può essere caricato
                    try {
                        model.setIsActive(false);
                        modelDatabaseService.saveModel(model);
                        log.warn("⚠️ Modello {} v{} disattivato a causa dell'errore di caricamento", 
                                model.getModelName(), model.getVersion());
                    } catch (Exception saveError) {
                        log.error("❌ Errore disattivazione modello: {}", saveError.getMessage());
                    }
                }
            }
            
            // === STATISTICHE FINALI ===
            log.info("🎯 Caricamento completato - Successi: {}, Fallimenti: {}", 
                    successCount, failureCount);
            
            if (successCount > 0) {
                logMemoryStatistics();
            }
            
        } catch (Exception e) {
            log.error("❌ Errore durante caricamento automatico modelli: {}", e.getMessage(), e);
        }
    }

    /**
     * Carica un singolo modello in memoria
     */
    private void loadModelIntoMemory(ModelEntity model) throws Exception {
        String modelName = model.getModelName();
        String version = model.getVersion();
        
        // === VERIFICA SE GIÀ IN MEMORIA ===
        if (ModelRegistry.exists(modelName, version)) {
            log.debug("⚡ Modello {} v{} già presente in memoria", modelName, version);
            return;
        }
        
        // === DOWNLOAD DA MINIO ===
        log.debug("📥 Download modello da MinIO: {}", model.getFilePath());
        
        try (InputStream modelStream = minioRepository.downloadModel(model.getFilePath())) {
            
            // === CREAZIONE HANDLER ===
            Object handler = modelService.createModelHandler(modelStream, model.getType().toString());
            
            // === REGISTRAZIONE IN CACHE ===
            ModelRegistry.register(modelName, model.getType().toString(), version, (IModelHandler) handler);
            
            log.debug("🧠 Modello {} v{} registrato in memoria", modelName, version);
            
        } catch (Exception e) {
            throw new RuntimeException("Errore caricamento modello " + modelName + " v" + version, e);
        }
    }

    /**
     * Stampa statistiche della memoria cache
     */
    private void logMemoryStatistics() {
        try {
            var cachedModels = ModelRegistry.getAllModels();
            log.info("📊 Modelli in memoria: {} attivi", cachedModels.size());
            
            // Raggruppa per tipo
            var typeStats = cachedModels.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    entry -> entry.getType(),
                    java.util.stream.Collectors.counting()
                ));
            
            typeStats.forEach((type, count) -> 
                log.info("   📈 {}: {} modelli", type, count));
                
        } catch (Exception e) {
            log.warn("⚠️ Errore durante stampa statistiche: {}", e.getMessage());
        }
    }

    /**
     * Metodo per ricaricare manualmente tutti i modelli attivi
     * Utile per operazioni di manutenzione
     */
    public void reloadAllActiveModels() {
        log.info("🔄 Ricaricamento manuale modelli attivi...");
        
        // Pulisce la cache esistente
        ModelRegistry.clear();
        log.info("🧹 Cache memoria pulita");
        
        // Ricarica tutti i modelli attivi
        loadActiveModelsOnStartup();
    }

    /**
     * Conta modelli attivi nel database vs memoria
     */
    public void auditModelsStatus() {
        try {
            List<ModelEntity> activeModels = modelDatabaseService.findActiveModels();
            var cachedModels = ModelRegistry.getAllModels();
            
            log.info("📊 Audit modelli:");
            log.info("   💾 Database attivi: {}", activeModels.size());
            log.info("   🧠 Memoria cache: {}", cachedModels.size());
            
            // Verifica coerenza
            long mismatch = activeModels.stream()
                .filter(model -> !ModelRegistry.exists(model.getModelName(), model.getVersion()))
                .count();
                
            if (mismatch > 0) {
                log.warn("⚠️ {} modelli attivi nel DB ma non in memoria", mismatch);
            } else {
                log.info("✅ Coerenza DB-Memoria verificata");
            }
            
        } catch (Exception e) {
            log.error("❌ Errore audit modelli: {}", e.getMessage());
        }
    }
}
