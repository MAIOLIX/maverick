package com.maiolix.maverick.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per le risposte di autenticazione token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta di autenticazione per client API")
public class TokenResponseDto {

    @Schema(description = "Token di accesso JWT", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    @Schema(description = "Tipo di token", example = "Bearer")
    private String tokenType;

    @Schema(description = "Durata del token in secondi", example = "86400")
    private Long expiresIn;

    @Schema(description = "Scope autorizzati per il client", example = "predict,schema")
    private String scope;
}
