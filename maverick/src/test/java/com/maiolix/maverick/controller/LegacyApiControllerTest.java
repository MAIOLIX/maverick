package com.maiolix.maverick.controller;

import com.maiolix.maverick.exception.ModelNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class LegacyApiControllerTest {

    private LegacyApiController legacyApiController;

    @BeforeEach
    void setUp() {
        legacyApiController = new LegacyApiController();
    }

    @Test
    void handleLegacyUpload_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyUpload()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/upload"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacyPredict_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyPredict()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/predict"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacySchema_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacySchema()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/schema"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacyInfo_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyInfo()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/info"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacyList_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyList()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/list"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacyAdd_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyAdd()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/add"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }

    @Test
    void handleLegacyRemove_ShouldThrowModelNotFoundExceptionWithCorrectMessage() {
        // When & Then
        ModelNotFoundException exception = assertThrows(
            ModelNotFoundException.class,
            () -> legacyApiController.handleLegacyRemove()
        );
        
        assertTrue(exception.getMessage().contains("/api/v1/models/remove"));
        assertTrue(exception.getMessage().contains("Please use the new API endpoint"));
    }
}
