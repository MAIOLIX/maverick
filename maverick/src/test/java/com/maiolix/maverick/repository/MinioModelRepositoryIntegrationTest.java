package com.maiolix.maverick.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test di integrazione per MinioModelRepository
 * Testa le operazioni essenziali con MinIO server
 */
@SpringBootTest
@TestPropertySource(properties = {
    "maverick.storage.minio.endpoint=http://localhost:9000",
    "maverick.storage.minio.access-key=minioadmin",
    "maverick.storage.minio.secret-key=minioadmin",
    "maverick.storage.minio.default-bucket=maverick-test",
    "logging.level.com.maiolix.maverick=DEBUG"
})
@TestMethodOrder(OrderAnnotation.class)
class MinioModelRepositoryIntegrationTest {

    @Autowired
    private MinioModelRepository minioRepository;

    private static final String TEST_MODEL_NAME = "test-minio-model";
    private static final String TEST_VERSION = "v1.0";
    private static final String TEST_FILE_NAME = "model.onnx";

    /**
     * Test connessione MinIO
     */
    @Test
    @Order(1)
    void testMinioConnection() {
        assertDoesNotThrow(() -> {
            minioRepository.testConnection();
        }, "Connessione a MinIO dovrebbe riuscire");

        assertNotNull(minioRepository.getDefaultBucket(), "Default bucket dovrebbe essere configurato");
        assertNotNull(minioRepository.getEndpoint(), "Endpoint dovrebbe essere configurato");
    }

    /**
     * Test workflow: Upload → Download → Delete
     */
    @Test
    @Order(2)
    void testUploadDownloadDeleteWorkflow() {
        // === PREPARAZIONE DATI ===
        String testContent = "Mock ONNX model content for MinIO integration testing";
        byte[] contentBytes = testContent.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(contentBytes);

        // === TEST UPLOAD ===
        String objectPath = generateObjectPath(TEST_MODEL_NAME, TEST_VERSION, TEST_FILE_NAME);
        
        assertDoesNotThrow(() -> {
            minioRepository.uploadModel(TEST_MODEL_NAME, TEST_VERSION, TEST_FILE_NAME, 
                    inputStream, contentBytes.length, "application/octet-stream");
        }, "Upload dovrebbe riuscire senza eccezioni");

        // === TEST DOWNLOAD ===
        InputStream downloadedStream = assertDoesNotThrow(() -> {
            return minioRepository.downloadModel(objectPath);
        }, "Download dovrebbe riuscire senza eccezioni");

        assertNotNull(downloadedStream, "Download dovrebbe restituire uno stream valido");

        // Verifica contenuto
        String downloadedContent = assertDoesNotThrow(() -> {
            return new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
        }, "Lettura stream dovrebbe riuscire");

        assertEquals(testContent, downloadedContent, "Contenuto scaricato dovrebbe corrispondere");

        assertDoesNotThrow(downloadedStream::close, "Chiusura stream dovrebbe riuscire");

        // === TEST DELETE ===
        boolean deleted = assertDoesNotThrow(() -> {
            return minioRepository.deleteModel(objectPath);
        }, "Delete dovrebbe riuscire senza eccezioni");

        assertTrue(deleted, "Delete dovrebbe restituire true per successo");
    }

    /**
     * Test gestione errori
     */
    @Test
    @Order(3)
    void testErrorHandling() {
        String nonExistentPath = "non-existent-model/v1.0/model.onnx";

        // === TEST DOWNLOAD FILE INESISTENTE ===
        assertThrows(Exception.class, () -> {
            minioRepository.downloadModel(nonExistentPath);
        }, "Download di file inesistente dovrebbe lanciare eccezione");

        // === TEST DELETE FILE INESISTENTE ===
        boolean deleteResult = assertDoesNotThrow(() -> {
            return minioRepository.deleteModel(nonExistentPath);
        }, "Delete di file inesistente non dovrebbe lanciare eccezione");

        assertFalse(deleteResult, "Delete di file inesistente dovrebbe restituire false");

        // === TEST UPLOAD CON STREAM NULLO ===
        assertThrows(Exception.class, () -> {
            minioRepository.uploadModel(TEST_MODEL_NAME, TEST_VERSION, TEST_FILE_NAME, 
                    null, 0, "application/octet-stream");
        }, "Upload con stream null dovrebbe lanciare eccezione");
    }

    /**
     * Test con file di dimensioni diverse
     */
    @Test
    @Order(4)
    void testDifferentFileSizes() {
        // === TEST FILE PICCOLO (1KB) ===
        testFileUploadDownload("small-file", 1024);

        // === TEST FILE MEDIO (10KB) ===
        testFileUploadDownload("medium-file", 10 * 1024);

        // === TEST FILE GRANDE (100KB) ===
        testFileUploadDownload("large-file", 100 * 1024);
    }

    /**
     * Test gestione versioni multiple
     */
    @Test
    @Order(5)
    void testMultipleVersions() {
        String modelName = "versioning-test";
        String fileName = "model.onnx";

        // === UPLOAD VERSIONE 1 ===
        uploadAndVerify(modelName, "v1.0", fileName, "Version 1 content");

        // === UPLOAD VERSIONE 2 ===
        uploadAndVerify(modelName, "v2.0", fileName, "Version 2 content");

        // === VERIFICA DOWNLOAD VERSIONI SEPARATE ===
        String v1Path = generateObjectPath(modelName, "v1.0", fileName);
        String v2Path = generateObjectPath(modelName, "v2.0", fileName);

        String v1Content = downloadAndReadContent(v1Path);
        String v2Content = downloadAndReadContent(v2Path);

        assertEquals("Version 1 content", v1Content, "Versione 1 dovrebbe avere contenuto corretto");
        assertEquals("Version 2 content", v2Content, "Versione 2 dovrebbe avere contenuto corretto");

        // === CLEANUP ===
        assertDoesNotThrow(() -> {
            minioRepository.deleteModel(v1Path);
            minioRepository.deleteModel(v2Path);
        }, "Cleanup dovrebbe riuscire");
    }

    /**
     * Test performance operazioni multiple
     */
    @Test
    @Order(6)
    void testMultipleOperationsPerformance() {
        String modelName = "performance-test";
        int numberOfFiles = 5;

        long startTime = System.currentTimeMillis();

        // === UPLOAD MULTIPLI ===
        for (int i = 0; i < numberOfFiles; i++) {
            String version = "v1." + i;
            String fileName = "model-" + i + ".onnx";
            String content = "Performance test content for file " + i;
            
            uploadAndVerify(modelName, version, fileName, content);
        }

        // === DOWNLOAD E VERIFICA ===
        for (int i = 0; i < numberOfFiles; i++) {
            String version = "v1." + i;
            String fileName = "model-" + i + ".onnx";
            String objectPath = generateObjectPath(modelName, version, fileName);
            
            String content = downloadAndReadContent(objectPath);
            assertEquals("Performance test content for file " + i, content);
        }

        // === DELETE MULTIPLI ===
        for (int i = 0; i < numberOfFiles; i++) {
            String version = "v1." + i;
            String fileName = "model-" + i + ".onnx";
            String objectPath = generateObjectPath(modelName, version, fileName);
            
            boolean deleted = assertDoesNotThrow(() -> {
                return minioRepository.deleteModel(objectPath);
            });
            assertTrue(deleted, "Delete dovrebbe riuscire per file " + i);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        assertTrue(totalTime < 15000, "Operazioni multiple dovrebbero completare in meno di 15 secondi");
        System.out.println("Performance test completato in " + totalTime + "ms per " + numberOfFiles + " file");
    }

    // === METODI HELPER ===

    private void testFileUploadDownload(String testName, int fileSize) {
        String modelName = "size-test-" + testName;
        String version = "v1.0";
        String fileName = "model.onnx";

        // Crea contenuto di dimensione specifica
        StringBuilder contentBuilder = new StringBuilder();
        String baseContent = "Test content for " + testName + " - ";
        while (contentBuilder.length() < fileSize) {
            contentBuilder.append(baseContent);
        }
        String content = contentBuilder.substring(0, fileSize);

        // Upload, verifica e cleanup
        uploadAndVerify(modelName, version, fileName, content);
        
        String objectPath = generateObjectPath(modelName, version, fileName);
        String downloadedContent = downloadAndReadContent(objectPath);
        assertEquals(content, downloadedContent, "Contenuto dovrebbe corrispondere per " + testName);
        
        // Cleanup
        assertDoesNotThrow(() -> {
            minioRepository.deleteModel(objectPath);
        });
    }

    private void uploadAndVerify(String modelName, String version, String fileName, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(contentBytes);
        
        assertDoesNotThrow(() -> {
            minioRepository.uploadModel(modelName, version, fileName, 
                    inputStream, contentBytes.length, "application/octet-stream");
        }, "Upload dovrebbe riuscire per " + modelName + "/" + version);
    }

    private String downloadAndReadContent(String objectPath) {
        return assertDoesNotThrow(() -> {
            InputStream stream = minioRepository.downloadModel(objectPath);
            String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();
            return content;
        }, "Download e lettura dovrebbero riuscire per " + objectPath);
    }

    private String generateObjectPath(String modelName, String version, String fileName) {
        return String.format("%s/%s/%s", modelName, version, fileName);
    }
}
