package com.maiolix.maverick.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect old endpoints to new API paths
        registry.addRedirectViewController("/models/upload", "/api/v1/models/upload");
        registry.addRedirectViewController("/models/predict/**", "/api/v1/models/predict/**");
        registry.addRedirectViewController("/models/schema/**", "/api/v1/models/schema/**");
        registry.addRedirectViewController("/models/info/**", "/api/v1/models/info/**");
        registry.addRedirectViewController("/models/list", "/api/v1/models/list");
    }
}
