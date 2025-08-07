package com.maiolix.maverick.dto.admin;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di creazione utente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta per la creazione di un nuovo utente")
public class CreateUserResponseDto {

    @Schema(description = "ID del nuovo utente", example = "123")
    private Long id;

    @Schema(description = "Nome utente", example = "johndoe")
    private String username;

    @Schema(description = "Email dell'utente", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Nome dell'utente", example = "John")
    private String firstName;

    @Schema(description = "Cognome dell'utente", example = "Doe")
    private String lastName;

    @Schema(description = "Ruolo assegnato", example = "ADMIN")
    private String role;

    @Schema(description = "Stato attivo", example = "true")
    private Boolean isActive;

    @Schema(description = "Data e ora di creazione", example = "2024-08-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Messaggio di conferma", example = "Utente creato con successo")
    private String message;
}
