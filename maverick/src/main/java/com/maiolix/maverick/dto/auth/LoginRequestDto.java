package com.maiolix.maverick.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per le richieste di login degli utenti
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta di login per utenti umani")
public class LoginRequestDto {

    @NotBlank(message = "Username è obbligatorio")
    @Schema(description = "Username dell'utente", example = "admin")
    private String username;

    @NotBlank(message = "Password è obbligatoria")
    @Schema(description = "Password dell'utente", example = "password123")
    private String password;
}
