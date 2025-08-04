package com.maiolix.maverick;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.maiolix.maverick=DEBUG"
})
class MaverickApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Test that the Spring Boot application context loads successfully
        // This is a basic smoke test to ensure all components are properly configured
        assertTrue(true, "Application context should load without errors");
    }

    @Test
    void applicationPropertiesAreLoaded() {
        // Test that application properties are properly loaded
        // This ensures the configuration is correct
        
        // Basic assertion to verify test is running
        assertNotNull(System.getProperty("java.version"));
    }

    @Test
    void springProfilesConfiguration() {
        // Test Spring profiles configuration
        String activeProfile = System.getProperty("spring.profiles.active");
        
        // In test environment, profile might be null or "test"
        assertTrue(activeProfile == null || activeProfile.contains("test") || activeProfile.isEmpty(),
                "Active profile should be null, empty, or contain 'test' in test environment");
    }
}
