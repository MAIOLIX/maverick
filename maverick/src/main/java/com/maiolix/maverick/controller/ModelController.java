package com.maiolix.maverick.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.service.IModelService;
import com.maiolix.maverick.service.ModelServiceImpl.ModelNotFoundException;
import com.maiolix.maverick.service.ModelServiceImpl.ModelPredictionException;
import com.maiolix.maverick.service.ModelServiceImpl.ModelUploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
@Slf4j
public class ModelController {

    private final IModelService modelService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelName") String modelName,
            @RequestParam("type") String type,
            @RequestParam("version") String version) {
        
        try {
            modelService.uploadModel(file, modelName, type, version);
            log.info("Model uploaded successfully: {} version: {}", modelName, version);
            return ResponseEntity.ok("Model uploaded successfully: " + modelName + " version: " + version);
            
        } catch (ModelUploadException e) {
            log.error("Failed to upload model '{}': {}", modelName, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload model: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading model '{}': {}", modelName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error uploading model: " + e.getMessage());
        }
    }

    @PostMapping("/predict/{version}/{modelName}")
    public ResponseEntity<Object> predict(
            @PathVariable String modelName,
            @PathVariable String version,
            @RequestBody Object input) {
        
        try {
            Object prediction = modelService.predict(modelName, version, input);
            log.debug("Prediction completed for model: {} version: {}", modelName, version);
            return ResponseEntity.ok(prediction);
            
        } catch (ModelNotFoundException e) {
            log.error("Model not found: {} version: {}", modelName, version);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Model not found: " + modelName + " version: " + version);
        } catch (ModelPredictionException e) {
            log.error("Prediction error for model '{}' version '{}': {}", modelName, version, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Prediction error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during prediction for model '{}' version '{}': {}", 
                    modelName, version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error during prediction: " + e.getMessage());
        }
    }
}