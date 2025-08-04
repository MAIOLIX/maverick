package com.maiolix.maverick.registry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.maiolix.maverick.handler.IModelHandler;

class ModelRegistryTest {

    @Mock
    private IModelHandler mockHandler1;

    @Mock
    private IModelHandler mockHandler2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ModelRegistry.clear(); // Clean slate for each test
    }

    @Test
    void testRegisterAndGet() {
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        
        ModelCacheEntry entry = ModelRegistry.get("test-model", "1.0");
        
        assertNotNull(entry);
        assertEquals("test-model", entry.getModelName());
        assertEquals("ONNX", entry.getType());
        assertEquals("1.0", entry.getVersion());
        assertEquals(mockHandler1, entry.getHandler());
    }

    @Test
    void testGetByKey() {
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        String key = ModelCacheEntry.generateKey("test-model", "1.0");
        
        ModelCacheEntry entry = ModelRegistry.getByKey(key);
        
        assertNotNull(entry);
        assertEquals("test-model", entry.getModelName());
        assertEquals("ONNX", entry.getType());
        assertEquals("1.0", entry.getVersion());
        assertEquals(mockHandler1, entry.getHandler());
    }

    @Test
    void testGetNonExistentModel() {
        ModelCacheEntry entry = ModelRegistry.get("non-existent", "1.0");
        assertNull(entry);
    }

    @Test
    void testExists() {
        assertFalse(ModelRegistry.exists("test-model", "1.0"));
        
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        
        assertTrue(ModelRegistry.exists("test-model", "1.0"));
        assertFalse(ModelRegistry.exists("test-model", "2.0"));
        assertFalse(ModelRegistry.exists("other-model", "1.0"));
    }

    @Test
    void testExistsByKey() {
        String key = ModelCacheEntry.generateKey("test-model", "1.0");
        
        assertFalse(ModelRegistry.existsByKey(key));
        
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        
        assertTrue(ModelRegistry.existsByKey(key));
    }

    @Test
    void testRemove() {
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        assertTrue(ModelRegistry.exists("test-model", "1.0"));
        
        ModelCacheEntry removedEntry = ModelRegistry.remove("test-model", "1.0");
        
        assertNotNull(removedEntry);
        assertEquals("test-model", removedEntry.getModelName());
        assertFalse(ModelRegistry.exists("test-model", "1.0"));
    }

    @Test
    void testRemoveNonExistentModel() {
        ModelCacheEntry removedEntry = ModelRegistry.remove("non-existent", "1.0");
        assertNull(removedEntry);
    }

    @Test
    void testGetAllModels() {
        assertTrue(ModelRegistry.getAllModels().isEmpty());
        
        ModelRegistry.register("model1", "ONNX", "1.0", mockHandler1);
        ModelRegistry.register("model2", "MOJO", "2.0", mockHandler2);
        
        Collection<ModelCacheEntry> allModels = ModelRegistry.getAllModels();
        assertEquals(2, allModels.size());
        
        // Verify models are present
        boolean found1 = allModels.stream().anyMatch(entry -> 
            "model1".equals(entry.getModelName()) && "1.0".equals(entry.getVersion()));
        boolean found2 = allModels.stream().anyMatch(entry -> 
            "model2".equals(entry.getModelName()) && "2.0".equals(entry.getVersion()));
        
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testSize() {
        assertEquals(0, ModelRegistry.size());
        
        ModelRegistry.register("model1", "ONNX", "1.0", mockHandler1);
        assertEquals(1, ModelRegistry.size());
        
        ModelRegistry.register("model2", "MOJO", "2.0", mockHandler2);
        assertEquals(2, ModelRegistry.size());
        
        ModelRegistry.remove("model1", "1.0");
        assertEquals(1, ModelRegistry.size());
    }

    @Test
    void testClear() {
        ModelRegistry.register("model1", "ONNX", "1.0", mockHandler1);
        ModelRegistry.register("model2", "MOJO", "2.0", mockHandler2);
        assertEquals(2, ModelRegistry.size());
        
        ModelRegistry.clear();
        
        assertEquals(0, ModelRegistry.size());
        assertTrue(ModelRegistry.getAllModels().isEmpty());
        assertFalse(ModelRegistry.exists("model1", "1.0"));
        assertFalse(ModelRegistry.exists("model2", "2.0"));
    }

    @Test
    void testMultipleVersionsOfSameModel() {
        ModelRegistry.register("test-model", "ONNX", "1.0", mockHandler1);
        ModelRegistry.register("test-model", "ONNX", "2.0", mockHandler2);
        
        assertEquals(2, ModelRegistry.size());
        assertTrue(ModelRegistry.exists("test-model", "1.0"));
        assertTrue(ModelRegistry.exists("test-model", "2.0"));
        
        ModelCacheEntry entry1 = ModelRegistry.get("test-model", "1.0");
        ModelCacheEntry entry2 = ModelRegistry.get("test-model", "2.0");
        
        assertNotNull(entry1);
        assertNotNull(entry2);
        assertEquals("1.0", entry1.getVersion());
        assertEquals("2.0", entry2.getVersion());
        assertEquals(mockHandler1, entry1.getHandler());
        assertEquals(mockHandler2, entry2.getHandler());
    }
}
