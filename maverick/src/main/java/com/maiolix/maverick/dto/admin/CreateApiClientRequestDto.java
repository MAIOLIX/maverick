package com.maiolix.maverick.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la richiesta di creazione di un nuovo client API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta per la creazione di un nuovo client API")
public class CreateApiClientRequestDto {

    @NotBlank(message = "Client ID è obbligatorio")
    @Size(min = 3, max = 50, message = "Client ID deve essere tra 3 e 50 caratteri")
    @Schema(description = "Identificativo univoco del client", example = "mobile-app-client")
    private String clientId;

    @NotBlank(message = "Client Secret è obbligatorio")
    @Size(min = 16, max = 100, message = "Client Secret deve essere tra 16 e 100 caratteri")
    @Schema(description = "Secret del client (minimo 16 caratteri)", example = "super-secret-key-123456")
    private String clientSecret;

    @NotBlank(message = "Nome è obbligatorio")
    @Size(min = 3, max = 100, message = "Nome deve essere tra 3 e 100 caratteri")
    @Schema(description = "Nome descrittivo del client", example = "Mobile Application Client")
    private String name;

    @Schema(description = "Descrizione del client", example = "Client per l'applicazione mobile iOS/Android")
    private String description;

    @NotNull(message = "Admin access è obbligatorio")
    @Schema(description = "Se il client ha accesso amministrativo completo", example = "false")
    private Boolean adminAccess;

    @Schema(description = "Limite di richieste per minuto (opzionale, default basato su adminAccess)", 
            example = "100")
    private Integer rateLimitPerMinute;
}
