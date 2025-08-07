package com.maiolix.maverick.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${swagger.server.url:}")
    private String swaggerServerUrl;

    @Value("${swagger.server.description:Development Server}")
    private String swaggerServerDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo())
                .components(buildComponents())
                .addSecurityItem(buildSecurityRequirement());

        // Configurazione server dinamica per Docker
        if (!swaggerServerUrl.isEmpty()) {
            // URL specifico configurato (produzione/Docker)
            Server server = new Server();
            server.setUrl(swaggerServerUrl);
            server.setDescription(swaggerServerDescription);
            openAPI.servers(List.of(server));
        } else {
            // Auto-detect per sviluppo locale
            Server localServer = new Server();
            localServer.setUrl("http://localhost:" + serverPort);
            localServer.setDescription("Development Server");
            openAPI.servers(List.of(localServer));
        }

        return openAPI;
    }

    private Info buildInfo() {
        Contact contact = new Contact();
        contact.setEmail("contact@maiolix.com");
        contact.setName("Maiolix Team");
        contact.setUrl("https://www.maiolix.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        return new Info()
                .title("Maverick Platform API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                           **Maverick Platform** - Sistema completo di gestione modelli ML e amministrazione utenti.
                           
                           ## 🤖 Gestione Modelli ML
                           Supporta i seguenti formati di modelli:
                           - **MOJO**: Modelli H2O
                           - **ONNX**: Open Neural Network Exchange
                           - **PMML**: Predictive Model Markup Language
                           
                           ## 👑 Amministrazione
                           - Gestione utenti e ruoli
                           - Gestione client API
                           - Controllo accessi basato su ruoli
                           
                           ## 🔐 Autenticazione
                           L'API utilizza JWT (JSON Web Token) per l'autenticazione:
                           1. **Login**: POST `/api/auth/login` con username/password
                           2. **Token**: Usa il token ricevuto nell'header `Authorization: Bearer <token>`
                           3. **Ruoli**: USER, ADMIN, API_CLIENT con diversi livelli di accesso
                           
                           ### Funzionalità principali:
                           - Upload e gestione modelli ML
                           - Predizioni real-time
                           - Schema di input/output
                           - Cache dei modelli in memoria
                           - Gestione amministrativa completa
                           - Autenticazione multi-livello
                           """)
                .termsOfService("http://swagger.io/terms/")
                .license(license);
    }

    private Components buildComponents() {
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(BEARER_AUTH)
                .description("Inserisci il token JWT ottenuto dal login");

        return new Components()
                .addSecuritySchemes(BEARER_AUTH, jwtSecurityScheme);
    }

    private SecurityRequirement buildSecurityRequirement() {
        return new SecurityRequirement().addList(BEARER_AUTH);
    }
}
