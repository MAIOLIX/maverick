package com.maiolix.maverick.handler;

import java.util.Map;

public interface IModelHandler {
    Object predict(Object input);
    
    /**
     * Gets information about the input schema required by the model
     * @return Map containing input metadata (names, types, shapes, etc.)
     */
    Map<String, Object> getInputSchema();
}
