package com.maiolix.maverick.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.maiolix.maverick.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configurazione di sicurezza per autenticazione JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "maverick.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disabilita CSRF per API stateless
            .csrf(csrf -> csrf.disable())
            
            // Disabilita session management (stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurazione autorizzazioni
            .authorizeHttpRequests(auth -> auth
                // Endpoint pubblici di autenticazione
                .requestMatchers("/api/auth/**").permitAll()
                
                // Debug endpoint (DA RIMUOVERE IN PRODUZIONE)
                .requestMatchers("/api/debug/**").permitAll()
                
                // Endpoint di health e monitoring (opzionali)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // API documentation (Swagger) - solo in sviluppo
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // Upload modelli - solo ADMIN
                .requestMatchers("/api/models/upload/**").hasRole("ADMIN")
                .requestMatchers("/api/models/delete/**").hasRole("ADMIN")
                .requestMatchers("/api/models/manage/**").hasRole("ADMIN")
                
                // Prediction - sia ADMIN che PREDICTOR
                .requestMatchers("/api/models/predict/**").hasAnyRole("ADMIN", "PREDICTOR")
                
                // Schema endpoints - sia ADMIN che PREDICTOR
                .requestMatchers("/api/models/*/input-schema").hasAnyRole("ADMIN", "PREDICTOR")
                .requestMatchers("/api/models/*/output-schema").hasAnyRole("ADMIN", "PREDICTOR")
                
                // Lista modelli - sia ADMIN che PREDICTOR
                .requestMatchers("/api/models/list").hasAnyRole("ADMIN", "PREDICTOR")
                
                // Tutti gli altri endpoint richiedono autenticazione
                .anyRequest().authenticated()
            )
            
            // Aggiungi il filtro JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
