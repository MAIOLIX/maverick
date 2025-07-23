package com.maiolix.maverick.registry;

import java.util.concurrent.ConcurrentHashMap;

import com.maiolix.maverick.handler.IModelHandler;

public class ModelRegistry {

    private ModelRegistry() {
        // Prevent instantiation
    }

    private static final ConcurrentHashMap<String, ModelCacheEntry> models = new ConcurrentHashMap<>();

    public static void register(String name, String type, IModelHandler handler) {
        models.put(name, new ModelCacheEntry(name, type, handler));
    }

    public static ModelCacheEntry get(String name) {
        return models.get(name);
    }

    public static boolean exists(String name) {
        return models.containsKey(name);
    }
}