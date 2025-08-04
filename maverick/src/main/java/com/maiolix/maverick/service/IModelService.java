package com.maiolix.maverick.service;

import org.springframework.web.multipart.MultipartFile;

public interface IModelService {
    void uploadModel(MultipartFile file, String modelName, String type, String version);
    
    /**
     * Execute prediction using a specific model version
     * @param modelName the name of the model
     * @param version the version of the model
     * @param input the input data for prediction
     * @return prediction result
     */
    Object predict(String modelName, String version, Object input);
    
    /**
     * Get input schema for a specific model version
     * @param modelName the name of the model
     * @param version the version of the model
     * @return input schema information
     */
    Object getInputSchema(String modelName, String version);
    
    /**
     * Get model information for a specific model version
     * @param modelName the name of the model
     * @param version the version of the model
     * @return model metadata and information
     */
    Object getModelInfo(String modelName, String version);
    
    /**
     * Add a model to the registry
     * @param modelName the name of the model
     * @param type the type of the model (ONNX, MOJO, PMML, etc.)
     * @param version the version of the model
     * @param handler the model handler instance
     */
    void addModel(String modelName, String type, String version, Object handler);
    
    /**
     * Remove a model from the registry
     * @param modelName the name of the model
     * @param version the version of the model
     * @return true if the model was removed, false if not found
     */
    boolean removeModel(String modelName, String version);
    
    /**
     * Get all models currently in cache
     * @return list of all cached models with their metadata
     */
    Object getAllModels();
}
