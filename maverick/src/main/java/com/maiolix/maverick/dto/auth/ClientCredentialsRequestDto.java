package com.maiolix.maverick.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per le richieste di autenticazione client credentials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta di autenticazione per client API")
public class ClientCredentialsRequestDto {

    @NotBlank(message = "Client ID è obbligatorio")
    @Schema(description = "ID del client API", example = "maverick-client-001")
    private String clientId;

    @NotBlank(message = "Client secret è obbligatorio")
    @Schema(description = "Secret del client API", example = "super-secret-key-123")
    private String clientSecret;

    @Schema(description = "Grant type (deve essere 'client_credentials')", example = "client_credentials")
    private String grantType = "client_credentials";
}
