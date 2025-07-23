package com.maiolix.maverick.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.service.ModelServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelServiceImpl modelService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadModel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelName") String modelName,
            @RequestParam("type") String type,
            @RequestParam("version") String version) {
        modelService.uploadModel(file, modelName, type, version);
        return ResponseEntity.ok("Modello caricato: " + modelName);
    }

    @PostMapping("/predict/{modelName}")
    public ResponseEntity<Object> predict(
            @PathVariable String modelName,
            @RequestBody Object input) {
        Object prediction;
        try {
            prediction = modelService.predict(modelName, input);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Modello non trovato: " + modelName);
        }
        return ResponseEntity.ok(prediction);
    }
}