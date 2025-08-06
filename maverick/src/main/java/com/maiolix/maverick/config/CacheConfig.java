package com.maiolix.maverick.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione per abilitare il caching
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // La configurazione della cache è gestita tramite application.properties
}
