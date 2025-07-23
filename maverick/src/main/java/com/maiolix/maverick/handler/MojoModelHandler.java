package com.maiolix.maverick.handler;

import java.io.InputStream;
import java.util.Map;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

public class MojoModelHandler implements IModelHandler {

    private final EasyPredictModelWrapper model;

    public MojoModelHandler(InputStream mojoStream) throws Exception {
        // Save InputStream to a temporary file
        java.io.File tempFile = java.io.File.createTempFile("mojoModel", ".zip");
        tempFile.deleteOnExit();
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = mojoStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        MojoModel mojoModel = MojoModel.load(tempFile.getAbsolutePath());
        this.model = new EasyPredictModelWrapper(mojoModel);
    }

    @Override
    public Object predict(Object input) {
        try {
            Map<String, Object> inputMap = (Map<String, Object>) input;

            RowData row = new RowData();
            inputMap.forEach((k, v) -> row.put(k, v.toString()));

            MultinomialModelPrediction prediction = model.predictMultinomial(row);

            return Map.of(
                    "predictedClass", prediction.label,
                    "classProbabilities", prediction.classProbabilities
            );
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la predizione MOJO", e);
        }
    }
}


