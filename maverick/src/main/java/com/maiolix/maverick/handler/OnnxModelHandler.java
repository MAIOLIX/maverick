package com.maiolix.maverick.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.maiolix.maverick.exception.OnnxModelException;
import com.maiolix.maverick.exception.OnnxPredictionException;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

@SuppressWarnings("java:S2139") // Sonar warning for exception handling - we properly log and rethrow
public class OnnxModelHandler implements IModelHandler {
    private static final Logger LOGGER = Logger.getLogger(OnnxModelHandler.class.getName());
    
    private final OrtEnvironment env;
    private final OrtSession session;
    private Path tempModelFile;

    public OnnxModelHandler(InputStream modelStream) throws OnnxModelException {
        if (modelStream == null) {
            throw new OnnxModelException("Model stream cannot be null");
        }
        
        try {
            this.env = OrtEnvironment.getEnvironment();
            this.tempModelFile = Files.createTempFile("model", ".onnx");
            Files.copy(modelStream, tempModelFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            this.session = env.createSession(tempModelFile.toString(), new OrtSession.SessionOptions());
            LOGGER.log(Level.INFO, "ONNX model loaded successfully from temporary file: {0}", tempModelFile);
        } catch (IOException e) {
            // Log and rethrow with context - this is the correct pattern for constructor failures
            LOGGER.log(Level.SEVERE, "Failed to create temporary file for ONNX model", e);
            throw new OnnxModelException("Failed to create temporary file for ONNX model", e);
        } catch (OrtException e) {
            // Log, cleanup and rethrow with context
            LOGGER.log(Level.SEVERE, "Failed to create ONNX session", e);
            cleanup();
            throw new OnnxModelException("Failed to create ONNX session", e);
        } catch (Exception e) {
            // Log, cleanup and rethrow with context for unexpected errors
            LOGGER.log(Level.SEVERE, "Unexpected error during ONNX model initialization", e);
            cleanup();
            throw new OnnxModelException("Unexpected error during ONNX model initialization", e);
        }
    }

    @Override
    public Object predict(Object input) {
        if (input == null) {
            throw new OnnxPredictionException("Input cannot be null");
        }
        
        if (!(input instanceof Map)) {
            throw new OnnxPredictionException("Input must be a Map<String, Object>");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> inputMap = (Map<String, Object>) input;
        
        if (inputMap.isEmpty()) {
            throw new OnnxPredictionException("Input map cannot be empty");
        }
        
        OnnxTensor tensor = null;
        OrtSession.Result result = null;
        
        try {
            // Validazione e conversione dei dati di input
            double[] doubleData = validateAndConvertInput(inputMap);
            float[] data = convertToFloatArray(doubleData);

            // Creazione tensor
            tensor = OnnxTensor.createTensor(env, new float[][]{data});
            
            // Esecuzione predizione
            String inputName = getFirstInputName();
            result = session.run(Map.of(inputName, tensor));
            
            // Elaborazione risultato
            return OnnxUtils.processResult(result);
            
        } catch (NumberFormatException e) {
            // Log and rethrow with context - specific handling for number format errors
            LOGGER.log(Level.WARNING, "Invalid number format in input data", e);
            throw new OnnxPredictionException("Invalid number format in input data: " + e.getMessage(), e);
        } catch (OrtException e) {
            // Log and rethrow with context - specific handling for ONNX runtime errors
            LOGGER.log(Level.SEVERE, "ONNX runtime error during prediction", e);
            throw new OnnxPredictionException("ONNX runtime error during prediction", e);
        } catch (Exception e) {
            // Log and rethrow with context - catch-all for unexpected errors
            LOGGER.log(Level.SEVERE, "Unexpected error during ONNX prediction", e);
            throw new OnnxPredictionException("Unexpected error during prediction", e);
        } finally {
            // Cleanup delle risorse
            cleanupResources(tensor, result);
        }
    }
    
    private double[] validateAndConvertInput(Map<String, Object> inputMap) {
        try {
            return inputMap.values().stream()
                    .mapToDouble(this::parseToDouble)
                    .toArray();
        } catch (Exception e) {
            throw new OnnxPredictionException("Failed to convert input values to numeric data", e);
        }
    }
    
    private double parseToDouble(Object value) {
        if (value == null) {
            throw new NumberFormatException("Input value cannot be null");
        }
        
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot convert value '" + value + "' to double");
        }
    }
    
    private float[] convertToFloatArray(double[] doubleData) {
        float[] data = new float[doubleData.length];
        for (int i = 0; i < doubleData.length; i++) {
            data[i] = (float) doubleData[i];
        }
        return data;
    }
    
    private String getFirstInputName() {
        if (session.getInputNames().isEmpty()) {
            throw new OnnxPredictionException("No input names found in ONNX model");
        }
        return session.getInputNames().iterator().next();
    }
    
    private void cleanupResources(OnnxTensor tensor, OrtSession.Result result) {
        try {
            if (tensor != null) {
                tensor.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error closing tensor", e);
        }
        
        try {
            if (result != null) {
                result.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error closing result", e);
        }
    }
    
    private void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error closing session", e);
        }
        
        try {
            if (tempModelFile != null && Files.exists(tempModelFile)) {
                Files.delete(tempModelFile);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting temporary model file", e);
        }
    }
    
    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        
        try {
            // Get input information from ONNX session
            Map<String, ai.onnxruntime.NodeInfo> inputsInfo = session.getInputInfo();
            
            // Process each input
            for (Map.Entry<String, ai.onnxruntime.NodeInfo> entry : inputsInfo.entrySet()) {
                String inputName = entry.getKey();
                ai.onnxruntime.NodeInfo nodeInfo = entry.getValue();
                schema.put(inputName, createInputDetails(inputName, nodeInfo));
            }
            
            // Add general information
            addGeneralInfo(schema, inputsInfo);
            
            // Add usage example
            addInputExample(schema);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error extracting input schema from ONNX model", e);
            schema.put("error", "Unable to extract input schema: " + e.getMessage());
        }
        
        return schema;
    }
    
    private Map<String, Object> createInputDetails(String inputName, ai.onnxruntime.NodeInfo nodeInfo) {
        Map<String, Object> inputDetails = new java.util.HashMap<>();
        inputDetails.put("name", inputName);
        
        if (nodeInfo.getInfo() instanceof ai.onnxruntime.TensorInfo tensorInfo) {
            addTensorInfo(inputDetails, tensorInfo);
        } else {
            inputDetails.put("type", nodeInfo.getInfo().toString());
        }
        
        return inputDetails;
    }
    
    private void addTensorInfo(Map<String, Object> inputDetails, ai.onnxruntime.TensorInfo tensorInfo) {
        inputDetails.put("dataType", tensorInfo.type.toString());
        inputDetails.put("shape", java.util.Arrays.toString(tensorInfo.getShape()));
        inputDetails.put("dimensions", tensorInfo.getShape().length);
        inputDetails.put("shapeArray", tensorInfo.getShape());
        
        // Add human-readable description
        String description = createDescription(tensorInfo);
        inputDetails.put("description", description);
        
        // Add expected input size for fixed dimensions
        addExpectedFeatures(inputDetails, tensorInfo.getShape(), tensorInfo.type.toString());
    }
    
    private String createDescription(ai.onnxruntime.TensorInfo tensorInfo) {
        StringBuilder description = new StringBuilder();
        description.append(tensorInfo.type.toString().toLowerCase()).append(" tensor");
        if (tensorInfo.getShape().length > 0) {
            description.append(" with shape ").append(java.util.Arrays.toString(tensorInfo.getShape()));
        }
        return description.toString();
    }
    
    private void addExpectedFeatures(Map<String, Object> inputDetails, long[] shape, String dataType) {
        if (shape.length > 1 && shape[1] > 0) {
            inputDetails.put("expectedFeatures", shape[1]);
            inputDetails.put("inputFormat", "Array of " + shape[1] + " " + dataType.toLowerCase() + " values");
        }
    }
    
    private void addGeneralInfo(Map<String, Object> schema, Map<String, ai.onnxruntime.NodeInfo> inputsInfo) {
        schema.put("totalInputs", inputsInfo.size());
        schema.put("inputNames", java.util.List.copyOf(session.getInputNames()));
        schema.put("modelType", "ONNX");
    }
    
    private void addInputExample(Map<String, Object> schema) {
        Map<String, Object> example = new java.util.HashMap<>();
        
        for (String inputName : session.getInputNames()) {
            Object inputInfo = schema.get(inputName);
            if (inputInfo instanceof Map<?, ?> inputMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedInputInfo = (Map<String, Object>) inputMap;
                Object expectedFeatures = typedInputInfo.get("expectedFeatures");
                if (expectedFeatures instanceof Long features) {
                    java.util.List<Double> exampleValues = createExampleValues(features.intValue());
                    example.put(inputName, exampleValues);
                }
            }
        }
        
        if (!example.isEmpty()) {
            schema.put("inputExample", example);
        }
    }
    
    private java.util.List<Double> createExampleValues(int count) {
        java.util.List<Double> values = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(0.0);
        }
        return values;
    }
    
    public void close() {
        cleanup();
        LOGGER.info("ONNX model handler closed and resources cleaned up");
    }
}
