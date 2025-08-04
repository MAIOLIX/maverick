package com.maiolix.maverick.controller;

import com.maiolix.maverick.exception.ModelNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling legacy API endpoints and providing appropriate redirects.
 * This controller ensures backward compatibility while guiding clients to use the new API paths.
 */
@RestController
@RequestMapping("/models")
public class LegacyApiController {

    private static final String API_V1_MODELS_BASE = "/api/v1/models";
    private static final String USE_NEW_ENDPOINT_MSG = "Please use the new API endpoint: ";

    @PostMapping("/upload")
    public ResponseEntity<String> handleLegacyUpload() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/upload");
    }

    @PostMapping("/predict/**")
    public ResponseEntity<String> handleLegacyPredict() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/predict/{modelName}");
    }

    @GetMapping("/schema/**")
    public ResponseEntity<String> handleLegacySchema() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/schema/{modelName}");
    }

    @GetMapping("/info/**")
    public ResponseEntity<String> handleLegacyInfo() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/info/{modelName}");
    }

    @GetMapping("/list")
    public ResponseEntity<String> handleLegacyList() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/list");
    }

    @PostMapping("/add")
    public ResponseEntity<String> handleLegacyAdd() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/add");
    }

    @PostMapping("/remove/**")
    public ResponseEntity<String> handleLegacyRemove() {
        throw new ModelNotFoundException(USE_NEW_ENDPOINT_MSG + API_V1_MODELS_BASE + "/remove/{modelName}");
    }
}
