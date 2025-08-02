package com.maiolix.maverick.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.maiolix.maverick.exception.MojoModelException;
import com.maiolix.maverick.exception.MojoPredictionException;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

@SuppressWarnings("java:S2139") // Sonar warning for exception handling - we properly log and rethrow
public class MojoModelHandler implements IModelHandler {
    private static final Logger LOGGER = Logger.getLogger(MojoModelHandler.class.getName());
    private static final String CATEGORICAL_TYPE = "categorical";
    private static final String NUMERIC_TYPE = "numeric";
    
    private final EasyPredictModelWrapper model;
    private File tempModelFile;

    public MojoModelHandler(InputStream mojoStream) throws MojoModelException {
        if (mojoStream == null) {
            throw new MojoModelException("Model stream cannot be null");
        }
        
        try {
            // Create temporary file for the model
            this.tempModelFile = File.createTempFile("mojoModel", ".zip");
            tempModelFile.deleteOnExit();
            
            // Save InputStream to temporary file
            try (FileOutputStream out = new FileOutputStream(tempModelFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = mojoStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // Load MOJO model
            MojoModel mojoModel = MojoModel.load(tempModelFile.getAbsolutePath());
            this.model = new EasyPredictModelWrapper(mojoModel);
            
            LOGGER.log(Level.INFO, "MOJO model loaded successfully from temporary file: {0}", tempModelFile);
            
        } catch (IOException e) {
            // Log and rethrow with context - specific handling for I/O errors
            LOGGER.log(Level.SEVERE, "Failed to create or write temporary file for MOJO model", e);
            cleanup();
            throw new MojoModelException("Failed to create or write temporary file for MOJO model", e);
        } catch (Exception e) {
            // Log, cleanup and rethrow with context for unexpected errors
            LOGGER.log(Level.SEVERE, "Unexpected error during MOJO model initialization", e);
            cleanup();
            throw new MojoModelException("Unexpected error during MOJO model initialization", e);
        }
    }

    @Override
    public Object predict(Object input) {
        if (input == null) {
            throw new MojoPredictionException("Input cannot be null");
        }
        
        if (!(input instanceof Map)) {
            throw new MojoPredictionException("Input must be a Map<String, Object>");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> inputMap = (Map<String, Object>) input;
        
        if (inputMap.isEmpty()) {
            throw new MojoPredictionException("Input map cannot be empty");
        }
        
        try {
            // Create row data from input map
            RowData row = createRowData(inputMap);
            
            // Execute prediction
            MultinomialModelPrediction prediction = model.predictMultinomial(row);
            
            // Process and return result
            return Map.of(
                    "predictedClass", prediction.label,
                    "classProbabilities", prediction.classProbabilities
            );
            
        } catch (Exception e) {
            // Log and rethrow with context - catch-all for prediction errors
            LOGGER.log(Level.SEVERE, "Unexpected error during MOJO prediction", e);
            throw new MojoPredictionException("Unexpected error during prediction", e);
        }
    }
    
    private RowData createRowData(Map<String, Object> inputMap) {
        try {
            RowData row = new RowData();
            inputMap.forEach((key, value) -> {
                if (value != null) {
                    row.put(key, value.toString());
                } else {
                    throw new MojoPredictionException("Input value for key '" + key + "' cannot be null");
                }
            });
            return row;
        } catch (Exception e) {
            throw new MojoPredictionException("Failed to create row data from input map", e);
        }
    }
    
    private void cleanup() {
        try {
            if (tempModelFile != null && tempModelFile.exists()) {
                Files.delete(tempModelFile.toPath());
                LOGGER.log(Level.INFO, "Temporary model file deleted: {0}", tempModelFile);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e, () -> "Error deleting temporary model file: " + tempModelFile);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error during cleanup of temporary model file", e);
        }
    }
    
    public void close() {
        cleanup();
        LOGGER.info("MOJO model handler closed and resources cleaned up");
    }
    
    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new java.util.HashMap<>();
        
        try {
            // Get underlying GenModel from wrapper
            hex.genmodel.GenModel genModel = model.m;
            
            // Extract feature names
            String[] featureNames = genModel.getNames();
            
            // Create detailed input information
            Map<String, Object> features = new java.util.HashMap<>();
            for (int i = 0; i < featureNames.length; i++) {
                Map<String, Object> featureInfo = new java.util.HashMap<>();
                featureInfo.put("index", i);
                featureInfo.put("name", featureNames[i]);
                
                // Check if categorical (has domain values)
                String[] domainValues = genModel.getDomainValues(i);
                if (domainValues != null && domainValues.length > 0) {
                    featureInfo.put("type", CATEGORICAL_TYPE);
                    featureInfo.put("domainValues", java.util.Arrays.asList(domainValues));
                } else {
                    featureInfo.put("type", NUMERIC_TYPE);
                }
                
                features.put(featureNames[i], featureInfo);
            }
            
            schema.put("features", features);
            schema.put("totalFeatures", featureNames.length);
            schema.put("featureNames", java.util.Arrays.asList(featureNames));
            
            // Add model metadata
            schema.put("modelCategory", genModel.getModelCategory().toString());
            schema.put("responseColumnName", genModel.getResponseName());
            
            // Add output information
            if (genModel.isSupervised()) {
                schema.put("supervised", true);
                schema.put("nClasses", genModel.getNumResponseClasses());
                
                if (genModel.getNumResponseClasses() > 1) {
                    String[] responseNames = genModel.getDomainValues(genModel.getResponseName());
                    if (responseNames != null) {
                        schema.put("responseClasses", java.util.Arrays.asList(responseNames));
                    }
                }
            } else {
                schema.put("supervised", false);
            }
            
            // Add model type
            schema.put("modelType", "MOJO");
            
            // Add input example
            addInputExample(schema, features, featureNames);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error extracting input schema from MOJO model", e);
            schema.put("error", "Unable to extract input schema: " + e.getMessage());
        }
        
        return schema;
    }
    
    private void addInputExample(Map<String, Object> schema, Map<String, Object> features, String[] featureNames) {
        Map<String, Object> inputExample = new java.util.HashMap<>();
        
        for (String featureName : featureNames) {
            Object featureInfo = features.get(featureName);
            if (featureInfo instanceof Map<?, ?> featureMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedFeatureMap = (Map<String, Object>) featureMap;
                
                String type = (String) typedFeatureMap.get("type");
                if (CATEGORICAL_TYPE.equals(type)) {
                    // For categorical features, use the first domain value as example
                    addCategoricalExample(inputExample, featureName, typedFeatureMap);
                } else {
                    // For numeric features, use 0.0 as example
                    inputExample.put(featureName, 0.0);
                }
            }
        }
        
        if (!inputExample.isEmpty()) {
            schema.put("inputExample", inputExample);
            addUsageInstructions(schema, features, featureNames);
        }
    }
    
    private void addCategoricalExample(Map<String, Object> inputExample, String featureName, Map<String, Object> featureMap) {
        @SuppressWarnings("unchecked")
        java.util.List<String> domainValues = (java.util.List<String>) featureMap.get("domainValues");
        if (domainValues != null && !domainValues.isEmpty()) {
            inputExample.put(featureName, domainValues.get(0));
        } else {
            inputExample.put(featureName, "category_value");
        }
    }
    
    private void addUsageInstructions(Map<String, Object> schema, Map<String, Object> features, String[] featureNames) {
        Map<String, Object> usageInstructions = new java.util.HashMap<>();
        usageInstructions.put("format", "JSON object with feature names as keys");
        usageInstructions.put("note", "Use exact feature names and appropriate data types");
        
        // Add specific instructions for categorical features
        java.util.List<String> categoricalFeatures = getCategoricalFeatures(features, featureNames);
        
        if (!categoricalFeatures.isEmpty()) {
            usageInstructions.put("categoricalFeatures", categoricalFeatures);
            usageInstructions.put("categoricalNote", "Use exact values from domainValues for categorical features");
        }
        
        schema.put("usageInstructions", usageInstructions);
    }
    
    private java.util.List<String> getCategoricalFeatures(Map<String, Object> features, String[] featureNames) {
        java.util.List<String> categoricalFeatures = new java.util.ArrayList<>();
        for (String featureName : featureNames) {
            Object featureInfo = features.get(featureName);
            if (featureInfo instanceof Map<?, ?> featureMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedFeatureMap = (Map<String, Object>) featureMap;
                if (CATEGORICAL_TYPE.equals(typedFeatureMap.get("type"))) {
                    categoricalFeatures.add(featureName);
                }
            }
        }
        return categoricalFeatures;
    }
}