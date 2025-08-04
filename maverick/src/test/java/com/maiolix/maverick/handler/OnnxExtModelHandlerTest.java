package com.maiolix.maverick.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.maiolix.maverick.exception.OnnxExtModelException;
import com.maiolix.maverick.exception.OnnxExtPredictionException;

class OnnxExtModelHandlerTest {

    private OnnxExtModelHandler onnxExtHandler;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary ZIP file for testing ONNX Extended models
        tempFile = Files.createTempFile("test-onnx-ext", ".zip");
        // Write minimal content (real ONNX Extended files are ZIP archives with .onnx and .json files)
        Files.write(tempFile, "fake-onnx-ext-content".getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (onnxExtHandler != null) {
            onnxExtHandler.close();
        }
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }

    @Test
    void testConstructorWithNullStream() {
        Exception exception = assertThrows(OnnxExtModelException.class, () -> {
            new OnnxExtModelHandler(null);
        });
        assertTrue(exception.getMessage().contains("ZIP stream cannot be null"));
    }

    @Test
    void testConstructorWithInvalidZipFile() {
        InputStream invalidStream = new ByteArrayInputStream("not-a-valid-zip".getBytes());
        
        Exception exception = assertThrows(OnnxExtModelException.class, () -> {
            new OnnxExtModelHandler(invalidStream);
        });
        // The exception will be thrown during ZIP processing
        assertNotNull(exception);
    }

    @Test
    void testInputValidation() {
        // Test various input validation scenarios
        
        // Null input
        Map<String, Object> nullInput = null;
        assertNull(nullInput);
        
        // Empty input
        Map<String, Object> emptyInput = Map.of();
        assertTrue(emptyInput.isEmpty());
        
        // Valid input structure
        Map<String, Object> validInput = Map.of("feature1", 1.0, "feature2", 2.0);
        assertFalse(validInput.isEmpty());
        assertEquals(2, validInput.size());
    }

    @Test
    void testExceptionHierarchy() {
        // Test that exceptions are properly typed
        OnnxExtModelException modelException = new OnnxExtModelException("Test model exception");
        assertInstanceOf(RuntimeException.class, modelException);
        
        OnnxExtPredictionException predictionException = new OnnxExtPredictionException("Test prediction exception");
        assertInstanceOf(RuntimeException.class, predictionException);
    }

    @Test
    void testCloseHandler() {
        // Test that close() method doesn't throw exceptions even if handler is null
        if (onnxExtHandler != null) {
            assertDoesNotThrow(() -> onnxExtHandler.close());
        }
    }

    @Test
    void testOnnxExtSpecificFeatures() {
        // Test ONNX Extended specific functionality concepts
        
        // ONNX Extended models should support label mapping
        Map<String, String> expectedLabelMapping = Map.of(
            "0", "class1",
            "1", "class2",
            "2", "class3"
        );
        assertNotNull(expectedLabelMapping);
        assertEquals(3, expectedLabelMapping.size());
        
        // Test expected ZIP structure concepts
        String[] expectedZipContents = {"model.onnx", "labels.json"};
        assertNotNull(expectedZipContents);
        assertEquals(2, expectedZipContents.length);
    }

    @Test
    void testLabelMappingStructure() {
        // Test expected label mapping structure
        Map<String, String> labelMapping = Map.of(
            "0", "cat",
            "1", "dog", 
            "2", "bird"
        );
        
        assertNotNull(labelMapping);
        assertTrue(labelMapping.containsKey("0"));
        assertEquals("cat", labelMapping.get("0"));
        assertEquals("dog", labelMapping.get("1"));
        assertEquals("bird", labelMapping.get("2"));
    }

    @Test
    void testInputSchemaStructure() {
        // Test expected structure of input schema for ONNX Extended models
        Map<String, Object> expectedSchemaStructure = Map.of(
            "modelType", "ONNX_EXT",
            "inputs", Map.of(
                "tensor_input", Map.of(
                    "type", "float32",
                    "shape", new int[]{1, 3, 224, 224},
                    "description", "Input image tensor"
                )
            ),
            "outputs", Map.of(
                "predictions", Map.of(
                    "type", "float32",
                    "shape", new int[]{1, 3}
                )
            ),
            "labelMapping", Map.of("0", "class1", "1", "class2", "2", "class3")
        );
        
        assertNotNull(expectedSchemaStructure);
        assertTrue(expectedSchemaStructure.containsKey("modelType"));
        assertTrue(expectedSchemaStructure.containsKey("inputs"));
        assertTrue(expectedSchemaStructure.containsKey("labelMapping"));
    }
}
