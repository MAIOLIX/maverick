package com.maiolix.maverick.handler;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
public class OnnxModelHandler implements IModelHandler {
 private final OrtEnvironment env;
    private final OrtSession session;

    public OnnxModelHandler(InputStream modelStream) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        Path tempFile = Files.createTempFile("model", ".onnx");
        Files.copy(modelStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        this.session = env.createSession(tempFile.toString(), new OrtSession.SessionOptions());
    }

    @Override
    public Object predict(Object input) {
        try {
            Map<String, Object> inputMap = (Map<String, Object>) input;
            // Costruisci tensor di input (esempio semplice con float[])
            double[] doubleData = inputMap.values().stream()
                    .mapToDouble(o -> Double.parseDouble(o.toString()))
                    .toArray();
            float[] data = new float[doubleData.length];
            for (int i = 0; i < doubleData.length; i++) {
                data[i] = (float) doubleData[i];
            }

            OnnxTensor tensor = OnnxTensor.createTensor(env, new float[][]{data});
            OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), tensor));
            Map<String,Object> jsonResponse=OnnxUtils.processResult(result);
            return jsonResponse;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la predizione ONNX", e);
        }
    }
}
