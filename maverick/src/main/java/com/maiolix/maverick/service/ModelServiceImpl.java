package com.maiolix.maverick.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.exception.MojoModelException;
import com.maiolix.maverick.exception.OnnxExtModelException;
import com.maiolix.maverick.exception.OnnxModelException;
import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.handler.MojoModelHandler;
import com.maiolix.maverick.handler.OnnxExtModelHandler;
import com.maiolix.maverick.handler.OnnxModelHandler;
import com.maiolix.maverick.handler.PmmlModelHandler;
import com.maiolix.maverick.registry.ModelCacheEntry;
import com.maiolix.maverick.registry.ModelRegistry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelServiceImpl implements IModelService {

    // Constants for JSON keys to avoid string duplication
    private static final String MODEL_NAME_KEY = "modelName";
    private static final String VERSION_KEY = "version";
    private static final String HAS_LABEL_MAPPING_KEY = "hasLabelMapping";
    private static final String MODEL_TYPE_KEY = "type";
    private static final String MODEL_KEY = "key";

    @Override
    public void uploadModel(MultipartFile file, String modelName, String type, String version) {
        // Validate input parameters
        validateUploadParameters(file, modelName, type, version);
        
        log.info("Uploading model: {} type: {} version: {}", modelName, type, version);
        
        IModelHandler handler = null;
        try {
            handler = createModelHandler(file, type);
            ModelRegistry.register(modelName, type, version, handler);
            log.info("Model {} successfully uploaded and registered", modelName);
            
        } catch (OnnxModelException e) {
            log.error("Failed to load ONNX model '{}': {}", modelName, e.getMessage(), e);
            throw new ModelUploadException("Failed to load ONNX model '" + modelName + "'", e);
        } catch (OnnxExtModelException e) {
            log.error("Failed to load ONNX Extended model '{}': {}", modelName, e.getMessage(), e);
            throw new ModelUploadException("Failed to load ONNX Extended model '" + modelName + "'", e);
        } catch (MojoModelException e) {
            log.error("Failed to load MOJO model '{}': {}", modelName, e.getMessage(), e);
            throw new ModelUploadException("Failed to load MOJO model '" + modelName + "'", e);
        } catch (IOException e) {
            log.error("I/O error while uploading model '{}': {}", modelName, e.getMessage(), e);
            throw new ModelUploadException("I/O error while uploading model '" + modelName + "'", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid model type '{}' for model '{}'", type, modelName, e);
            throw new ModelUploadException("Invalid model type '" + type + "' for model '" + modelName + "'", e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading model '{}' of type '{}': {}", modelName, type, e.getMessage(), e);
            throw new ModelUploadException("Unexpected error while uploading model '" + modelName + "' of type '" + type + "'", e);
        }
    }

    @Override
    public Object predict(String modelName, String version, Object input) {
        // Validate input parameters
        validatePredictParameters(modelName, version, input);
        
        log.debug("Executing prediction for model: {} version: {}", modelName, version);
        
        try {
            var entry = ModelRegistry.get(modelName, version);
            if (entry == null) {
                throw new ModelNotFoundException("Model not found: " + modelName + " version: " + version);
            }
            
            Object result = entry.getHandler().predict(input);
            log.debug("Prediction completed successfully for model: {} version: {}", modelName, version);
            return result;
            
        } catch (ModelNotFoundException e) {
            log.error("Model not found: {} version: {}", modelName, version);
            throw e;
        } catch (Exception e) {
            log.error("Error during prediction for model '{}' version '{}': {}", modelName, version, e.getMessage(), e);
            throw new ModelPredictionException("Error during prediction for model '" + modelName + "' version '" + version + "'", e);
        }
    }
    
    private void validateUploadParameters(MultipartFile file, String modelName, String type, String version) {
        if (file == null || file.isEmpty()) {
            throw new ModelUploadException("Model file cannot be null or empty");
        }
        
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelUploadException("Model name cannot be null or empty");
        }
        
        if (type == null || type.trim().isEmpty()) {
            throw new ModelUploadException("Model type cannot be null or empty");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new ModelUploadException("Model version cannot be null or empty");
        }
    }
    
    private void validatePredictParameters(String modelName, String version, Object input) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelPredictionException("Model name cannot be null or empty");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new ModelPredictionException("Model version cannot be null or empty");
        }
        
        if (input == null) {
            throw new ModelPredictionException("Input cannot be null");
        }
    }
    
    private IModelHandler createModelHandler(MultipartFile file, String type) 
            throws IOException {
        return switch (type.toUpperCase()) {
            case "ONNX" -> new OnnxModelHandler(file.getInputStream());
            case "ONNX_EXT" -> new OnnxExtModelHandler(file.getInputStream());
            case "MOJO" -> new MojoModelHandler(file.getInputStream());
            case "PMML" -> new PmmlModelHandler(file.getInputStream());
            default -> throw new IllegalArgumentException("Unsupported model type: " + type + ". Supported types: ONNX, ONNX_EXT, MOJO, PMML");
        };
    }
    
    @Override
    public Object getInputSchema(String modelName, String version) {
        String key = ModelCacheEntry.generateKey(modelName, version);
        
        if (!ModelRegistry.existsByKey(key)) {
            throw new ModelNotFoundException("Model not found: " + modelName + " version: " + version);
        }
        
        try {
            ModelCacheEntry cacheEntry = ModelRegistry.getByKey(key);
            IModelHandler handler = cacheEntry.getHandler();
            
            // Get input schema from handler
            Map<String, Object> schema = handler.getInputSchema();
            
            // Add additional metadata
            Map<String, Object> completeSchema = new java.util.HashMap<>(schema);
            completeSchema.put(MODEL_NAME_KEY, modelName);
            completeSchema.put("modelVersion", version);
            completeSchema.put("modelType", cacheEntry.getType());
            
            log.info("Input schema retrieved for model: {} version: {}", modelName, version);
            return completeSchema;
            
        } catch (Exception e) {
            log.error("Error retrieving input schema for model '{}' version '{}'", modelName, version, e);
            throw new ModelPredictionException("Error retrieving input schema for model '" + modelName + "' version '" + version + "'", e);
        }
    }
    
    @Override
    public Object getModelInfo(String modelName, String version) {
        String key = ModelCacheEntry.generateKey(modelName, version);
        
        if (!ModelRegistry.existsByKey(key)) {
            throw new ModelNotFoundException("Model not found: " + modelName + " version: " + version);
        }
        
        try {
            ModelCacheEntry cacheEntry = ModelRegistry.getByKey(key);
            
            // Create comprehensive model information
            Map<String, Object> modelInfo = new java.util.HashMap<>();
            modelInfo.put(MODEL_NAME_KEY, modelName);
            modelInfo.put(VERSION_KEY, version);
            modelInfo.put(MODEL_TYPE_KEY, cacheEntry.getType());
            modelInfo.put(MODEL_KEY, key);
            
            // Add label mapping if available
            if (cacheEntry.hasLabelMapping()) {
                modelInfo.put("labelMapping", cacheEntry.getLabelMapping());
                modelInfo.put(HAS_LABEL_MAPPING_KEY, true);
            } else {
                modelInfo.put(HAS_LABEL_MAPPING_KEY, false);
            }
            
            // Get input schema
            IModelHandler handler = cacheEntry.getHandler();
            Map<String, Object> inputSchema = handler.getInputSchema();
            modelInfo.put("inputSchema", inputSchema);
            
            log.info("Model info retrieved for model: {} version: {}", modelName, version);
            return modelInfo;
            
        } catch (Exception e) {
            log.error("Error retrieving model info for '{}' version '{}'", modelName, version, e);
            throw new ModelPredictionException("Error retrieving model info for '" + modelName + "' version '" + version + "'", e);
        }
    }
    
    @Override
    public void addModel(String modelName, String type, String version, Object handler) {
        // Validate input parameters
        validateAddModelParameters(modelName, type, version, handler);
        
        log.info("Adding model to registry: {} type: {} version: {}", modelName, type, version);
        
        try {
            if (!(handler instanceof IModelHandler)) {
                throw new IllegalArgumentException("Handler must be an instance of IModelHandler");
            }
            
            IModelHandler modelHandler = (IModelHandler) handler;
            ModelRegistry.register(modelName, type, version, modelHandler);
            log.info("Model {} successfully added to registry", modelName);
            
        } catch (Exception e) {
            log.error("Error adding model '{}' version '{}' to registry: {}", modelName, version, e.getMessage(), e);
            throw new ModelUploadException("Error adding model '" + modelName + "' version '" + version + "' to registry", e);
        }
    }
    
    @Override
    public boolean removeModel(String modelName, String version) {
        // Validate input parameters
        validateRemoveModelParameters(modelName, version);
        
        log.info("Removing model from registry: {} version: {}", modelName, version);
        
        try {
            var removedEntry = ModelRegistry.remove(modelName, version);
            boolean wasRemoved = removedEntry != null;
            
            if (wasRemoved) {
                log.info("Model {} version {} successfully removed from registry", modelName, version);
            } else {
                log.warn("Model {} version {} not found in registry", modelName, version);
            }
            
            return wasRemoved;
            
        } catch (Exception e) {
            log.error("Error removing model '{}' version '{}' from registry: {}", modelName, version, e.getMessage(), e);
            throw new ModelPredictionException("Error removing model '" + modelName + "' version '" + version + "' from registry", e);
        }
    }
    
    private void validateAddModelParameters(String modelName, String type, String version, Object handler) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelUploadException("Model name cannot be null or empty");
        }
        
        if (type == null || type.trim().isEmpty()) {
            throw new ModelUploadException("Model type cannot be null or empty");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new ModelUploadException("Model version cannot be null or empty");
        }
        
        if (handler == null) {
            throw new ModelUploadException("Model handler cannot be null");
        }
    }
    
    private void validateRemoveModelParameters(String modelName, String version) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelPredictionException("Model name cannot be null or empty");
        }
        
        if (version == null || version.trim().isEmpty()) {
            throw new ModelPredictionException("Model version cannot be null or empty");
        }
    }
    
    @Override
    public Object getModelsByName(String modelName) {
        log.info("Retrieving all versions for model: {}", modelName);
        
        // Validate input
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new ModelNotFoundException("Model name cannot be null or empty");
        }
        
        try {
            var allModels = ModelRegistry.getAllModels();
            
            // Filter models by name and transform to user-friendly format
            var modelList = allModels.stream()
                .filter(entry -> modelName.equals(entry.getModelName()))
                .map(entry -> {
                    Map<String, Object> modelInfo = new java.util.HashMap<>();
                    modelInfo.put(MODEL_NAME_KEY, entry.getModelName());
                    modelInfo.put(MODEL_TYPE_KEY, entry.getType());
                    modelInfo.put(VERSION_KEY, entry.getVersion());
                    modelInfo.put(MODEL_KEY, entry.getKey());
                    modelInfo.put(HAS_LABEL_MAPPING_KEY, entry.hasLabelMapping());
                    
                    // Add label mapping count if available
                    if (entry.hasLabelMapping()) {
                        modelInfo.put("labelMappingSize", entry.getLabelMapping().size());
                    }
                    
                    return modelInfo;
                })
                .sorted((a, b) -> ((String) a.get(VERSION_KEY)).compareTo((String) b.get(VERSION_KEY)))
                .toList();
            
            if (modelList.isEmpty()) {
                throw new ModelNotFoundException("No models found with name: " + modelName);
            }
            
            // Create summary response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put(MODEL_NAME_KEY, modelName);
            response.put("totalVersions", modelList.size());
            response.put("versions", modelList);
            
            log.info("Retrieved {} versions for model: {}", modelList.size(), modelName);
            return response;
            
        } catch (ModelNotFoundException e) {
            // Re-throw ModelNotFoundException to maintain proper error handling
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving models for name '{}': {}", modelName, e.getMessage(), e);
            throw new ModelPredictionException("Error retrieving models for name: " + modelName, e);
        }
    }
    
    @Override
    public Object getAllModels() {
        log.info("Retrieving all models from cache");
        
        try {
            var allModels = ModelRegistry.getAllModels();
            
            // Transform model cache entries to a more user-friendly format
            var modelList = allModels.stream()
                .map(entry -> {
                    Map<String, Object> modelInfo = new java.util.HashMap<>();
                    modelInfo.put(MODEL_NAME_KEY, entry.getModelName());
                    modelInfo.put(MODEL_TYPE_KEY, entry.getType());
                    modelInfo.put(VERSION_KEY, entry.getVersion());
                    modelInfo.put(MODEL_KEY, entry.getKey());
                    modelInfo.put(HAS_LABEL_MAPPING_KEY, entry.hasLabelMapping());
                    
                    // Add label mapping count if available
                    if (entry.hasLabelMapping()) {
                        modelInfo.put("labelMappingSize", entry.getLabelMapping().size());
                    }
                    
                    return modelInfo;
                })
                .sorted((a, b) -> {
                    // Sort by model name, then by version
                    int nameComparison = ((String) a.get(MODEL_NAME_KEY)).compareTo((String) b.get(MODEL_NAME_KEY));
                    if (nameComparison != 0) {
                        return nameComparison;
                    }
                    return ((String) a.get(VERSION_KEY)).compareTo((String) b.get(VERSION_KEY));
                })
                .toList();
            
            // Create summary response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("totalModels", allModels.size());
            response.put("models", modelList);
            
            log.info("Retrieved {} models from cache", allModels.size());
            return response;
            
        } catch (Exception e) {
            log.error("Error retrieving all models from cache: {}", e.getMessage(), e);
            throw new ModelPredictionException("Error retrieving all models from cache", e);
        }
    }
}