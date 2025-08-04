package com.maiolix.maverick.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.swagger.v3.oas.models.OpenAPI;

@ExtendWith(SpringExtension.class)
class SwaggerConfigTest {

    @Test
    void customOpenAPI_ShouldReturnConfiguredOpenAPI() {
        // Given
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();
        
        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Maverick ML Model Server API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getDescription());
        assertTrue(openAPI.getInfo().getDescription().contains("MOJO"));
        assertTrue(openAPI.getInfo().getDescription().contains("ONNX"));
        assertTrue(openAPI.getInfo().getDescription().contains("PMML"));
        
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("Maiolix Team", openAPI.getInfo().getContact().getName());
        assertEquals("contact@maiolix.com", openAPI.getInfo().getContact().getEmail());
        
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("Apache 2.0", openAPI.getInfo().getLicense().getName());
        
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertTrue(openAPI.getServers().get(0).getUrl().contains("localhost"));
    }
}
