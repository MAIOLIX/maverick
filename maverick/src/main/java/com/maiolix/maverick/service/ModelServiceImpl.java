package com.maiolix.maverick.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.handler.MojoModelHandler;
import com.maiolix.maverick.handler.MojoModelHandler.MojoModelException;
import com.maiolix.maverick.handler.OnnxModelHandler;
import com.maiolix.maverick.handler.OnnxModelHandler.OnnxModelException;
import com.maiolix.maverick.handler.PmmlModelHandler;
import com.maiolix.maverick.registry.ModelRegistry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelServiceImpl implements IModelService {

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
            throws IOException, OnnxModelException, MojoModelException {
        return switch (type.toUpperCase()) {
            case "ONNX" -> new OnnxModelHandler(file.getInputStream());
            case "MOJO" -> new MojoModelHandler(file.getInputStream());
            case "PMML" -> new PmmlModelHandler(file.getInputStream());
            default -> throw new IllegalArgumentException("Unsupported model type: " + type);
        };
    }
    
    // Custom exception classes
    public static class ModelUploadException extends RuntimeException {
        public ModelUploadException(String message) {
            super(message);
        }
        
        public ModelUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class ModelPredictionException extends RuntimeException {
        public ModelPredictionException(String message) {
            super(message);
        }
        
        public ModelPredictionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class ModelNotFoundException extends RuntimeException {
        public ModelNotFoundException(String message) {
            super(message);
        }
        
        public ModelNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}