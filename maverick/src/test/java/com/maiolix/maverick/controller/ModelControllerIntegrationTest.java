package com.maiolix.maverick.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.service.IModelService;

@WebMvcTest(ModelController.class)
class ModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IModelService modelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUploadModelEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.onnx", 
            "application/octet-stream", 
            "test content".getBytes()
        );

        doNothing().when(modelService).uploadModel(any(), eq("test-model"), eq("ONNX"), eq("1.0"));

        mockMvc.perform(multipart("/models/upload")
                .file(file)
                .param("modelName", "test-model")
                .param("type", "ONNX")
                .param("version", "1.0"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("uploaded successfully")));

        verify(modelService).uploadModel(any(), eq("test-model"), eq("ONNX"), eq("1.0"));
    }

    @Test
    void testUploadModelFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.onnx", 
            "application/octet-stream", 
            "test content".getBytes()
        );

        doThrow(new ModelUploadException("Upload failed"))
            .when(modelService).uploadModel(any(), eq("test-model"), eq("ONNX"), eq("1.0"));

        mockMvc.perform(multipart("/models/upload")
                .file(file)
                .param("modelName", "test-model")
                .param("type", "ONNX")
                .param("version", "1.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to upload model")));
    }

    @Test
    void testPredictEndpoint() throws Exception {
        Map<String, Object> input = Map.of("feature1", 1.0, "feature2", 2.0);
        Map<String, Object> expectedResult = Map.of("prediction", 0.85);

        when(modelService.predict("test-model", "1.0", input)).thenReturn(expectedResult);

        mockMvc.perform(post("/models/test-model/1.0/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").value(0.85));

        verify(modelService).predict("test-model", "1.0", input);
    }

    @Test
    void testPredictModelNotFound() throws Exception {
        Map<String, Object> input = Map.of("feature1", 1.0);

        when(modelService.predict("non-existent", "1.0", input))
            .thenThrow(new ModelNotFoundException("Model not found"));

        mockMvc.perform(post("/models/non-existent/1.0/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Model not found")));
    }

    @Test
    void testPredictWithInvalidInput() throws Exception {
        Map<String, Object> input = Map.of("feature1", 1.0);

        when(modelService.predict("test-model", "1.0", input))
            .thenThrow(new ModelPredictionException("Invalid input"));

        mockMvc.perform(post("/models/test-model/1.0/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid input")));
    }

    @Test
    void testGetInputSchemaEndpoint() throws Exception {
        Map<String, Object> expectedSchema = Map.of(
            "modelType", "ONNX",
            "inputs", Map.of("feature1", "float32")
        );

        when(modelService.getInputSchema("test-model", "1.0")).thenReturn(expectedSchema);

        mockMvc.perform(get("/models/test-model/1.0/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelType").value("ONNX"))
                .andExpect(jsonPath("$.inputs").exists());

        verify(modelService).getInputSchema("test-model", "1.0");
    }

    @Test
    void testGetModelInfoEndpoint() throws Exception {
        Map<String, Object> expectedInfo = Map.of(
            "modelName", "test-model",
            "version", "1.0",
            "type", "ONNX"
        );

        when(modelService.getModelInfo("test-model", "1.0")).thenReturn(expectedInfo);

        mockMvc.perform(get("/models/test-model/1.0/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("test-model"))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.type").value("ONNX"));

        verify(modelService).getModelInfo("test-model", "1.0");
    }

    @Test
    void testGetInputSchemaModelNotFound() throws Exception {
        when(modelService.getInputSchema("non-existent", "1.0"))
            .thenThrow(new ModelNotFoundException("Model not found"));

        mockMvc.perform(get("/models/non-existent/1.0/schema"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Model not found")));
    }

    @Test
    void testGetModelInfoModelNotFound() throws Exception {
        when(modelService.getModelInfo("non-existent", "1.0"))
            .thenThrow(new ModelNotFoundException("Model not found"));

        mockMvc.perform(get("/models/non-existent/1.0/info"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Model not found")));
    }
}
