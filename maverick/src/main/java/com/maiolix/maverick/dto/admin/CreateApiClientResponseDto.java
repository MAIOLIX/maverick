package com.maiolix.maverick.dto.admin;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di creazione client API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta per la creazione di un nuovo client API")
public class CreateApiClientResponseDto {

    @Schema(description = "ID del nuovo client", example = "456")
    private Long id;

    @Schema(description = "Client ID", example = "mobile-app-client")
    private String clientId;

    @Schema(description = "Nome del client", example = "Mobile Application Client")
    private String name;

    @Schema(description = "Descrizione del client", example = "Client per l'applicazione mobile iOS/Android")
    private String description;

    @Schema(description = "Accesso amministrativo", example = "false")
    private Boolean adminAccess;

    @Schema(description = "Scopes permessi", example = "predict,schema")
    private String allowedScopes;

    @Schema(description = "Limite richieste per minuto", example = "100")
    private Integer rateLimitPerMinute;

    @Schema(description = "Stato attivo", example = "true")
    private Boolean isActive;

    @Schema(description = "Data e ora di creazione", example = "2024-08-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Messaggio di conferma", example = "Client API creato con successo")
    private String message;

    @Schema(description = "Client Secret generato (mostrato solo alla creazione)", 
            example = "super-secret-key-123456")
    private String clientSecret;
}
