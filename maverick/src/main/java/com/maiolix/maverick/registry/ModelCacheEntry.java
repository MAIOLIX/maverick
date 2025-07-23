package com.maiolix.maverick.registry;

import com.maiolix.maverick.handler.IModelHandler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelCacheEntry {
    private String modelName;
    private String type; // MOJO / ONNX / PMML
    private IModelHandler handler;
}
