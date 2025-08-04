package com.maiolix.maverick.registry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.maiolix.maverick.handler.IModelHandler;

class ModelCacheEntryTest {

    @Mock
    private IModelHandler mockHandler;

    public ModelCacheEntryTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConstructorWithoutLabelMapping() {
        ModelCacheEntry entry = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        
        assertEquals("test-model", entry.getModelName());
        assertEquals("ONNX", entry.getType());
        assertEquals("1.0", entry.getVersion());
        assertEquals(mockHandler, entry.getHandler());
        assertNull(entry.getLabelMapping());
    }

    @Test
    void testConstructorWithLabelMapping() {
        Map<String, String> labelMapping = Map.of("0", "cat", "1", "dog");
        ModelCacheEntry entry = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler, labelMapping);
        
        assertEquals("test-model", entry.getModelName());
        assertEquals("ONNX", entry.getType());
        assertEquals("1.0", entry.getVersion());
        assertEquals(mockHandler, entry.getHandler());
        assertEquals(labelMapping, entry.getLabelMapping());
    }

    @Test
    void testGenerateKeyValid() {
        String key = ModelCacheEntry.generateKey("test-model", "1.0");
        assertEquals("test-model:1.0", key);
    }

    @Test
    void testGenerateKeyWithSpaces() {
        String key = ModelCacheEntry.generateKey("  test-model  ", "  1.0  ");
        assertEquals("test-model:1.0", key);
    }

    @Test
    void testGenerateKeyNullModelName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey(null, "1.0");
        });
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testGenerateKeyEmptyModelName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey("", "1.0");
        });
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testGenerateKeyBlankModelName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey("   ", "1.0");
        });
        assertTrue(exception.getMessage().contains("Model name cannot be null or empty"));
    }

    @Test
    void testGenerateKeyNullVersion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey("test-model", null);
        });
        assertTrue(exception.getMessage().contains("Version cannot be null or empty"));
    }

    @Test
    void testGenerateKeyEmptyVersion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey("test-model", "");
        });
        assertTrue(exception.getMessage().contains("Version cannot be null or empty"));
    }

    @Test
    void testGenerateKeyBlankVersion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ModelCacheEntry.generateKey("test-model", "   ");
        });
        assertTrue(exception.getMessage().contains("Version cannot be null or empty"));
    }

    @Test
    void testGetKey() {
        ModelCacheEntry entry = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        assertEquals("test-model:1.0", entry.getKey());
    }

    @Test
    void testSettersAndGetters() {
        ModelCacheEntry entry = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        
        // Test setters
        entry.setModelName("new-model");
        entry.setType("MOJO");
        entry.setVersion("2.0");
        Map<String, String> newLabelMapping = Map.of("0", "class1");
        entry.setLabelMapping(newLabelMapping);
        
        // Test getters
        assertEquals("new-model", entry.getModelName());
        assertEquals("MOJO", entry.getType());
        assertEquals("2.0", entry.getVersion());
        assertEquals(newLabelMapping, entry.getLabelMapping());
    }

    @Test
    void testEqualsAndHashCode() {
        ModelCacheEntry entry1 = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        ModelCacheEntry entry2 = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        ModelCacheEntry entry3 = new ModelCacheEntry("other-model", "ONNX", "1.0", mockHandler);
        
        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1, entry3);
    }

    @Test
    void testToString() {
        ModelCacheEntry entry = new ModelCacheEntry("test-model", "ONNX", "1.0", mockHandler);
        String toString = entry.toString();
        
        assertTrue(toString.contains("test-model"));
        assertTrue(toString.contains("ONNX"));
        assertTrue(toString.contains("1.0"));
    }
}
