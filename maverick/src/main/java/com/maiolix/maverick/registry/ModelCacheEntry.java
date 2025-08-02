package com.maiolix.maverick.registry;

import java.util.Map;

import com.maiolix.maverick.handler.IModelHandler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelCacheEntry {
    private String modelName;
    private String type; // MOJO / ONNX / PMML / ONNX_EXT
    private String version;
    private IModelHandler handler;
    private Map<String, String> labelMapping; // Optional label mapping
    
    /**
     * Constructor without label mapping
     */
    public ModelCacheEntry(String modelName, String type, String version, IModelHandler handler) {
        this.modelName = modelName;
        this.type = type;
        this.version = version;
        this.handler = handler;
        this.labelMapping = null;
    }
    
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
    
    /**
     * Checks if this model has label mapping
     * @return true if label mapping is available
     */
    public boolean hasLabelMapping() {
        return labelMapping != null && !labelMapping.isEmpty();
    }
    
    /**
     * Gets the label name for a given label index/key
     * @param labelKey the label key to lookup
     * @return label name or null if not found
     */
    public String getLabelName(String labelKey) {
        if (labelMapping == null) {
            return null;
        }
        return labelMapping.get(labelKey);
    }
}
