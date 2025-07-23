package com.maiolix.maverick.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.handler.IModelHandler;
import com.maiolix.maverick.handler.MojoModelHandler;
import com.maiolix.maverick.handler.OnnxModelHandler;
import com.maiolix.maverick.handler.PmmlModelHandler;
import com.maiolix.maverick.registry.ModelRegistry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModelServiceImpl implements IModelService {

    @Override
    public void uploadModel(MultipartFile file, String modelName, String type, String version) {
        log.info("Ricevuto modello: {} tipo: {} versione: {}", modelName, type, version);
        IModelHandler handler=null;
       try {
        handler = switch (type.toUpperCase()) {
        case "ONNX" -> new OnnxModelHandler(file.getInputStream());
        case "MOJO" -> new MojoModelHandler(file.getInputStream());
        case "PMML" -> new PmmlModelHandler(file.getInputStream());
        default -> throw new IllegalArgumentException("Tipo modello non supportato: " + type);
        };
      } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento del modello " + modelName + " di tipo " + type, e);
    }
        ModelRegistry.register(modelName, type, handler);
    }

    @Override
    public Object predict(String modelName, Object input) {
        var entry = ModelRegistry.get(modelName);
        if (entry == null) {
            throw new IllegalArgumentException("Modello non trovato: " + modelName);
        }
        return entry.getHandler().predict(input);
    }
}