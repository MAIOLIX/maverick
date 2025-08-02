package com.maiolix.maverick.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.service.IModelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
@Slf4j
public class ModelController {
    
    private static final String VERSION_SEPARATOR = " version: ";
    private static final String MODEL_NOT_FOUND_LOG = "Model not found: {} version: {}";
    private static final String MODEL_NOT_FOUND_MSG = "Model not found: ";

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
            return ResponseEntity.ok("Model uploaded successfully: " + modelName + VERSION_SEPARATOR + version);
            
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
            log.error(MODEL_NOT_FOUND_LOG, modelName, version);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MODEL_NOT_FOUND_MSG + modelName + VERSION_SEPARATOR + version);
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
    
    @GetMapping("/schema/{version}/{modelName}")
    public ResponseEntity<Object> getInputSchema(
            @PathVariable String modelName,
            @PathVariable String version) {
        
        try {
            Object schema = modelService.getInputSchema(modelName, version);
            log.debug("Input schema retrieved for model: {} version: {}", modelName, version);
            return ResponseEntity.ok(schema);
            
        } catch (ModelNotFoundException e) {
            log.error(MODEL_NOT_FOUND_LOG, modelName, version);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MODEL_NOT_FOUND_MSG + modelName + VERSION_SEPARATOR + version);
        } catch (Exception e) {
            log.error("Unexpected error retrieving schema for model '{}' version '{}': {}", 
                    modelName, version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error retrieving schema: " + e.getMessage());
        }
    }
    
    @GetMapping("/info/{version}/{modelName}")
    public ResponseEntity<Object> getModelInfo(
            @PathVariable String modelName,
            @PathVariable String version) {
        
        try {
            Object info = modelService.getModelInfo(modelName, version);
            log.debug("Model info retrieved for model: {} version: {}", modelName, version);
            return ResponseEntity.ok(info);
            
        } catch (ModelNotFoundException e) {
            log.error(MODEL_NOT_FOUND_LOG, modelName, version);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MODEL_NOT_FOUND_MSG + modelName + VERSION_SEPARATOR + version);
        } catch (Exception e) {
            log.error("Unexpected error retrieving info for model '{}' version '{}': {}", 
                    modelName, version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error retrieving model info: " + e.getMessage());
        }
    }
}