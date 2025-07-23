package com.maiolix.maverick.service;

import org.springframework.web.multipart.MultipartFile;

public interface IModelService {
    void uploadModel(MultipartFile file, String modelName, String type, String version);
    Object predict(String modelName, Object input);
}
