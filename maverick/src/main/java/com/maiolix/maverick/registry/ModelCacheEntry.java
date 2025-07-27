package com.maiolix.maverick.registry;

import com.maiolix.maverick.handler.IModelHandler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelCacheEntry {
    private String modelName;
    private String type; // MOJO / ONNX / PMML
    private String version;
    private IModelHandler handler;
    
    /**
     * Generates a unique key combining model name and version
     * @param modelName the name of the model
     * @param version the version of the model
     * @return unique key in format "modelName:version"
     */
    public static String generateKey(String modelName, String version) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        return modelName.trim() + ":" + version.trim();
    }
    
    /**
     * Gets the unique key for this model entry
     * @return unique key in format "modelName:version"
     */
    public String getKey() {
        return generateKey(this.modelName, this.version);
    }
}
