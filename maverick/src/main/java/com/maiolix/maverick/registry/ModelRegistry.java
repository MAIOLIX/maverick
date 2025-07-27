package com.maiolix.maverick.registry;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.maiolix.maverick.handler.IModelHandler;

public class ModelRegistry {

    private ModelRegistry() {
        // Prevent instantiation
    }

    private static final ConcurrentHashMap<String, ModelCacheEntry> models = new ConcurrentHashMap<>();

    /**
     * Register a model with a unique key based on name and version
     */
    public static void register(String name, String type, String version, IModelHandler handler) {
        String key = ModelCacheEntry.generateKey(name, version);
        ModelCacheEntry entry = new ModelCacheEntry(name, type, version, handler);
        models.put(key, entry);
    }

    /**
     * Get a model by name and version
     */
    public static ModelCacheEntry get(String name, String version) {
        String key = ModelCacheEntry.generateKey(name, version);
        return models.get(key);
    }

    /**
     * Get a model by unique key
     */
    public static ModelCacheEntry getByKey(String key) {
        return models.get(key);
    }

    /**
     * Check if a model exists by name and version
     */
    public static boolean exists(String name, String version) {
        String key = ModelCacheEntry.generateKey(name, version);
        return models.containsKey(key);
    }

    /**
     * Check if a model exists by unique key
     */
    public static boolean existsByKey(String key) {
        return models.containsKey(key);
    }

    /**
     * Remove a model by name and version
     */
    public static ModelCacheEntry remove(String name, String version) {
        String key = ModelCacheEntry.generateKey(name, version);
        return models.remove(key);
    }

    /**
     * Get all registered models
     */
    public static Collection<ModelCacheEntry> getAllModels() {
        return models.values();
    }

    /**
     * Get the number of registered models
     */
    public static int size() {
        return models.size();
    }

    /**
     * Clear all registered models
     */
    public static void clear() {
        models.clear();
    }
}