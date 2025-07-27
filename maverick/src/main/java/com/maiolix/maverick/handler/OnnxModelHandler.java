package com.maiolix.maverick.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    public void close() {
        cleanup();
        LOGGER.info("ONNX model handler closed and resources cleaned up");
    }
    
    // Classi di eccezione personalizzate
    public static class OnnxModelException extends Exception {
        public OnnxModelException(String message) {
            super(message);
        }
        
        public OnnxModelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class OnnxPredictionException extends RuntimeException {
        public OnnxPredictionException(String message) {
            super(message);
        }
        
        public OnnxPredictionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
