package com.maiolix.maverick.handler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxSequence;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtSession;

public class OnnxUtils {

    public static Map<String, Object> processResult(OrtSession.Result result) throws Exception {
        Map<String, Object> output = new LinkedHashMap<>();

        for (Map.Entry<String, OnnxValue> entry : result) {
            String outputName = entry.getKey();
            OnnxValue value = entry.getValue();

            if (value instanceof OnnxTensor tensor) {
                output.put(outputName, tensor.getValue());
            } else if (value instanceof OnnxSequence sequence) {
                List<Object> sequenceList = new ArrayList<>();

                for (Object item : sequence.getValue()) {
                    if (item instanceof Map<?, ?> mapItem) {
                        Map<String, Object> flatMap = new LinkedHashMap<>();
                        for (Map.Entry<?, ?> e : mapItem.entrySet()) {
                            flatMap.put(e.getKey().toString(), e.getValue());
                        }
                        sequenceList.add(flatMap);
                    } else {
                        sequenceList.add(item);
                    }
                }

                output.put(outputName, sequenceList);
            }
        }

        return output;
    }
}
