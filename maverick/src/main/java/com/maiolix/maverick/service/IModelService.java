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
}
