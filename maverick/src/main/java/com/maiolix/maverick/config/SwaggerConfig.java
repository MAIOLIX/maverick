package com.maiolix.maverick.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setEmail("contact@maiolix.com");
        contact.setName("Maiolix Team");
        contact.setUrl("https://www.maiolix.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Maverick ML Model Server API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                           API per la gestione e predizione di modelli di Machine Learning.
                           
                           Supporta i seguenti formati di modelli:
                           - **MOJO**: Modelli H2O
                           - **ONNX**: Open Neural Network Exchange
                           - **PMML**: Predictive Model Markup Language
                           
                           ### Funzionalità principali:
                           - Upload e gestione modelli
                           - Predizioni real-time
                           - Schema di input/output
                           - Informazioni sui modelli
                           - Cache dei modelli in memoria
                           """)
                .termsOfService("http://swagger.io/terms/")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
