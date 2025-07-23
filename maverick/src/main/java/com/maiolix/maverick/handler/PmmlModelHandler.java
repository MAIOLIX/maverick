package com.maiolix.maverick.handler;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class PmmlModelHandler implements IModelHandler {

    public PmmlModelHandler(InputStream pmmlStream) {
        // Mock: nessun caricamento effettivo
    }

    @Override
    public Object predict(Object input) {
        if (!(input instanceof Map)) {
            throw new IllegalArgumentException("Input deve essere di tipo Map<String, Object>");
        }

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("predictedClass", "mocked-class");
        output.put("classProbabilities", new double[]{0.8, 0.1, 0.1});
        return output;
    }
}
