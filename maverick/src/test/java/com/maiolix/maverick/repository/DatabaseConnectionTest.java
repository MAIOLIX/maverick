package com.maiolix.maverick.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.maiolix.maverick.entity.ModelEntity;

/**
 * Test semplice per verificare la connessione PostgreSQL
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseConnectionTest {

    @Autowired
    private ModelRepository modelRepository;

    @Test
    void testDatabaseConnection() {
        // Verifica che il repository sia correttamente iniettato
        assertThat(modelRepository).isNotNull();
        
        // Conta tutti i record nel database
        long count = modelRepository.count();
        
        // Il database dovrebbe essere accessibile (anche se vuoto)
        assertThat(count).isGreaterThanOrEqualTo(0);
        
        System.out.println("✅ Connessione PostgreSQL funzionante!");
        System.out.println("📊 Numero di modelli nel database: " + count);
    }

    @Test
    void testSaveSimpleModel() {
        // Crea un modello molto semplice per testare il salvataggio
        ModelEntity model = ModelEntity.builder()
                .modelName("test-connection")
                .version("1.0.0")
                .type(ModelEntity.ModelType.ONNX)
                .filePath("/test/path")
                .fileSize(1024L)
                .build();

        // Salva il modello
        ModelEntity saved = modelRepository.save(model);

        // Verifica che sia stato salvato
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getModelName()).isEqualTo("test-connection");
        
        // Verifica che possa essere recuperato
        Optional<ModelEntity> retrieved = modelRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getModelName()).isEqualTo("test-connection");
        
        System.out.println("✅ Salvataggio e recupero dal database funzionante!");
        System.out.println("🆔 ID modello salvato: " + saved.getId());
        System.out.println("📝 UUID generato: " + saved.getModelUuid());
    }
}
