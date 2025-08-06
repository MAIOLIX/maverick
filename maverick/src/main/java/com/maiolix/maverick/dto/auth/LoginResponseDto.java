package com.maiolix.maverick.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per le risposte di login degli utenti
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta di login per utenti umani")
public class LoginResponseDto {

    @Schema(description = "Token JWT per l'autenticazione", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    @Schema(description = "Tipo di token", example = "Bearer")
    private String tokenType;

    @Schema(description = "Durata del token in secondi", example = "900")
    private Long expiresIn;

    @Schema(description = "Username dell'utente", example = "admin")
    private String username;

    @Schema(description = "Email dell'utente", example = "admin@maverick.com")
    private String email;

    @Schema(description = "Ruolo dell'utente", example = "ADMIN")
    private String role;
}
