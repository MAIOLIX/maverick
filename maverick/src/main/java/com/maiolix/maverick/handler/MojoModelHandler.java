package com.maiolix.maverick.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

@SuppressWarnings("java:S2139") // Sonar warning for exception handling - we properly log and rethrow
public class MojoModelHandler implements IModelHandler {
    private static final Logger LOGGER = Logger.getLogger(MojoModelHandler.class.getName());
    
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
    
    // Custom exception classes
    public static class MojoModelException extends Exception {
        public MojoModelException(String message) {
            super(message);
        }
        
        public MojoModelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static class MojoPredictionException extends RuntimeException {
        public MojoPredictionException(String message) {
            super(message);
        }
        
        public MojoPredictionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


