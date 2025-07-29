package com.maiolix.maverick.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

@SuppressWarnings("java:S2139") // Sonar warning for exception handling - we properly log and rethrow
public class OnnxExtModelHandler implements IModelHandler {
    private static final Logger LOGGER = Logger.getLogger(OnnxExtModelHandler.class.getName());
    private static final String ONNX_FILE_EXTENSION = ".onnx";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String LABELS_JSON_FILENAME = "labels.json";
    
    private final OrtEnvironment env;
    private OrtSession session;  // Removed final to allow assignment in extractAndProcessZip
    private Path tempModelFile;
    private File tempZipFile;
    private Map<String, String> labelMapping;
    private final ObjectMapper objectMapper;

    public OnnxExtModelHandler(InputStream zipStream) throws OnnxExtModelException {
        if (zipStream == null) {
            throw new OnnxExtModelException("ZIP stream cannot be null");
        }
        
        this.objectMapper = new ObjectMapper();
        
        try {
            this.env = OrtEnvironment.getEnvironment();
            
            // Create temporary file for the ZIP
            this.tempZipFile = File.createTempFile("onnxExtModel", ".zip");
            tempZipFile.deleteOnExit();
            
            // Save ZIP stream to temporary file
            try (FileOutputStream out = new FileOutputStream(tempZipFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = zipStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // Extract and process ZIP contents
            extractAndProcessZip();
            
            LOGGER.log(Level.INFO, "ONNX Extended model loaded successfully from ZIP file: {0}", tempZipFile);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create or process temporary ZIP file for ONNX Extended model", e);
            cleanup();
            throw new OnnxExtModelException("Failed to create or process temporary ZIP file for ONNX Extended model", e);
        } catch (OrtException e) {
            LOGGER.log(Level.SEVERE, "Failed to create ONNX session", e);
            cleanup();
            throw new OnnxExtModelException("Failed to create ONNX session", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during ONNX Extended model initialization", e);
            cleanup();
            throw new OnnxExtModelException("Unexpected error during ONNX Extended model initialization", e);
        }
    }
    
    private void extractAndProcessZip() throws IOException, OrtException {
        Path onnxFile = null;
        Path jsonFile = null;
        
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(tempZipFile.toPath()))) {
            ZipEntry entry;
            
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                String fileName = entry.getName();
                String lowerFileName = fileName.toLowerCase();
                
                // Extract ONNX file
                if (lowerFileName.endsWith(ONNX_FILE_EXTENSION)) {
                    onnxFile = extractFileFromZip(zipIn, "onnx_model", ONNX_FILE_EXTENSION);
                    LOGGER.log(Level.INFO, "Extracted ONNX file: {0}", fileName);
                }
                // Extract JSON file (prioritize labels.json, but accept any .json)
                else if (lowerFileName.endsWith(JSON_FILE_EXTENSION) && 
                        (lowerFileName.equals(LABELS_JSON_FILENAME) || jsonFile == null)) {
                    jsonFile = extractFileFromZip(zipIn, "labels", JSON_FILE_EXTENSION);
                    LOGGER.log(Level.INFO, "Extracted JSON file: {0}", fileName);
                }
                
                zipIn.closeEntry();
            }
        }
        
        // Validate that both files were found
        if (onnxFile == null) {
            throw new IOException("No ONNX file found in ZIP archive");
        }
        
        if (jsonFile == null) {
            throw new IOException("No JSON file found in ZIP archive");
        }
        
        // Load label mapping from JSON
        loadLabelMapping(jsonFile);
        
        // Create ONNX session
        this.tempModelFile = onnxFile;
        this.session = env.createSession(onnxFile.toString(), new OrtSession.SessionOptions());
    }
    
    private Path extractFileFromZip(ZipInputStream zipIn, String prefix, String extension) throws IOException {
        Path tempFile = Files.createTempFile(prefix, extension);
        try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = zipIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
    
    private void loadLabelMapping(Path jsonFile) throws IOException {
        String jsonContent = Files.readString(jsonFile, StandardCharsets.UTF_8);
        
        // Try to parse as Map<String, String> first (string keys)
        if (tryParseAsStringMapping(jsonContent)) {
            return;
        }
        
        // Try to parse as Map<Integer, String> and convert keys to strings
        if (tryParseAsIntegerMapping(jsonContent)) {
            return;
        }
        
        throw new IOException("Unable to parse JSON file as label mapping. Expected format: {\"0\": \"class1\", \"1\": \"class2\", ...}");
    }
    
    private boolean tryParseAsStringMapping(String jsonContent) {
        try {
            TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
            this.labelMapping = objectMapper.readValue(jsonContent, typeRef);
            LOGGER.log(Level.INFO, "Loaded label mapping with {0} entries", labelMapping.size());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse JSON as Map<String, String>, trying Map<Integer, String>", e);
            return false;
        }
    }
    
    private boolean tryParseAsIntegerMapping(String jsonContent) {
        try {
            TypeReference<Map<Integer, String>> typeRef = new TypeReference<Map<Integer, String>>() {};
            Map<Integer, String> intMapping = objectMapper.readValue(jsonContent, typeRef);
            this.labelMapping = new java.util.HashMap<>();
            intMapping.forEach((key, value) -> this.labelMapping.put(String.valueOf(key), value));
            LOGGER.log(Level.INFO, "Loaded label mapping with {0} entries (converted from integer keys)", labelMapping.size());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse JSON as Map<Integer, String>", e);
            return false;
        }
    }

    @Override
    public Object predict(Object input) {
        if (input == null) {
            throw new OnnxExtPredictionException("Input cannot be null");
        }
        
        if (!(input instanceof Map)) {
            throw new OnnxExtPredictionException("Input must be a Map<String, Object>");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> inputMap = (Map<String, Object>) input;
        
        if (inputMap.isEmpty()) {
            throw new OnnxExtPredictionException("Input map cannot be empty");
        }
        
        OnnxTensor tensor = null;
        OrtSession.Result result = null;
        
        try {
            // Validation and conversion of input data
            double[] doubleData = validateAndConvertInput(inputMap);
            float[] data = convertToFloatArray(doubleData);

            // Create tensor
            tensor = OnnxTensor.createTensor(env, new float[][]{data});
            
            // Execute prediction
            String inputName = getFirstInputName();
            result = session.run(Map.of(inputName, tensor));
            
            // Process result with label mapping
            return processResultWithLabels(result);
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format in input data", e);
            throw new OnnxExtPredictionException("Invalid number format in input data: " + e.getMessage(), e);
        } catch (OrtException e) {
            LOGGER.log(Level.SEVERE, "ONNX runtime error during prediction", e);
            throw new OnnxExtPredictionException("ONNX runtime error during prediction", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during ONNX Extended prediction", e);
            throw new OnnxExtPredictionException("Unexpected error during prediction", e);
        } finally {
            // Cleanup resources
            cleanupResources(tensor, result);
        }
    }
    
    private Object processResultWithLabels(OrtSession.Result result) {
        try {
            // Get the raw result using OnnxUtils
            Map<String, Object> rawResult = OnnxUtils.processResult(result);
            
            // Enhance the result with label mapping if available
            if (labelMapping != null && !labelMapping.isEmpty()) {
                return enhanceResultWithLabels(rawResult);
            }
            
            return rawResult;
        } catch (Exception e) {
            throw new OnnxExtPredictionException("Failed to process result", e);
        }
    }
    
    private Map<String, Object> enhanceResultWithLabels(Map<String, Object> rawResult) {
        Map<String, Object> enhancedResult = new java.util.HashMap<>(rawResult);
        
        // Add label mapping information
        enhancedResult.put("labelMapping", labelMapping);
        
        // Add human-readable predicted class
        addPredictedClassName(enhancedResult, rawResult);
        
        // Add class probabilities with names
        addNamedClassProbabilities(enhancedResult, rawResult);
        
        return enhancedResult;
    }
    
    private void addPredictedClassName(Map<String, Object> enhancedResult, Map<String, Object> rawResult) {
        Object outputLabel = rawResult.get("output_label");
        if (outputLabel instanceof Object[] objectArray && objectArray.length > 0) {
            Object firstLabel = objectArray[0];
            if (firstLabel instanceof Number number) {
                String labelKey = String.valueOf(number.intValue());
                String className = labelMapping.get(labelKey);
                if (className != null) {
                    enhancedResult.put("predictedClassName", className);
                }
            }
        }
    }
    
    private void addNamedClassProbabilities(Map<String, Object> enhancedResult, Map<String, Object> rawResult) {
        Object outputProb = rawResult.get("output_probability");
        if (outputProb instanceof Object[] objectArray && objectArray.length > 0) {
            Object firstProb = objectArray[0];
            if (firstProb instanceof Map<?, ?> probMap) {
                Object value = probMap.get("value");
                if (value instanceof Map<?, ?> valueMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedValueMap = (Map<String, Object>) valueMap;
                    Map<String, Object> namedProbabilities = createNamedProbabilities(typedValueMap);
                    enhancedResult.put("classProbabilities", namedProbabilities);
                }
            }
        }
    }
    
    private Map<String, Object> createNamedProbabilities(Map<String, Object> valueMap) {
        Map<String, Object> namedProbabilities = new java.util.HashMap<>();
        
        valueMap.forEach((key, prob) -> {
            String className = labelMapping.get(key);
            if (className != null) {
                namedProbabilities.put(className, prob);
            } else {
                namedProbabilities.put("class_" + key, prob);
            }
        });
        
        return namedProbabilities;
    }
    
    private double[] validateAndConvertInput(Map<String, Object> inputMap) {
        try {
            return inputMap.values().stream()
                    .mapToDouble(this::parseToDouble)
                    .toArray();
        } catch (Exception e) {
            throw new OnnxExtPredictionException("Failed to convert input values to numeric data", e);
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
            throw new OnnxExtPredictionException("No input names found in ONNX model");
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
            LOGGER.log(Level.WARNING, "Error deleting temporary ONNX file", e);
        }
        
        try {
            if (tempZipFile != null && tempZipFile.exists()) {
                Files.delete(tempZipFile.toPath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error deleting temporary ZIP file", e);
        }
    }
    
    public void close() {
        cleanup();
        LOGGER.info("ONNX Extended model handler closed and resources cleaned up");
    }
    
    /**
     * Gets the label mapping loaded from the JSON file
     * @return map from label index to class name
     */
    public Map<String, String> getLabelMapping() {
        return labelMapping != null ? new java.util.HashMap<>(labelMapping) : new java.util.HashMap<>();
    }
    
    // Custom exception classes
    public static class OnnxExtModelException extends Exception {
        public OnnxExtModelException(String message) {
            super(message);
        }
        
        public OnnxExtModelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class OnnxExtPredictionException extends RuntimeException {
        public OnnxExtPredictionException(String message) {
            super(message);
        }
        
        public OnnxExtPredictionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
