package com.maiolix.maverick.dto.admin;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la visualizzazione delle informazioni client API con secret
 * ATTENZIONE: Usare solo per debug/admin, mai esporre in produzione
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informazioni dettagliate client API con secret (solo per admin)")
public class ApiClientDetailsDto {

    @Schema(description = "ID del client", example = "456")
    private Long id;

    @Schema(description = "Client ID", example = "mobile-app-prod")
    private String clientId;

    @Schema(description = "Nome del client", example = "Mobile Application")
    private String name;

    @Schema(description = "Descrizione del client", example = "Client per app mobile")
    private String description;

    @Schema(description = "Accesso amministrativo", example = "false")
    private Boolean adminAccess;

    @Schema(description = "Stato attivo", example = "true")
    private Boolean isActive;

    @Schema(description = "Scopes permessi", example = "predict,schema")
    private String allowedScopes;

    @Schema(description = "Limite richieste per minuto", example = "100")
    private Integer rateLimitPerMinute;

    @Schema(description = "Hash client secret BCrypt", example = "$2a$10$...")
    private String clientSecretHash;

    @Schema(description = "Data ultimo utilizzo", example = "2024-08-07T10:30:00")
    private LocalDateTime lastUsedAt;

    @Schema(description = "Numero totale utilizzi", example = "1337")
    private Long usageCount;

    @Schema(description = "Data e ora di creazione", example = "2024-08-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e ora ultima modifica", example = "2024-08-07T10:30:00")
    private LocalDateTime updatedAt;
}
