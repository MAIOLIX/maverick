package com.maiolix.maverick.dto.admin;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la visualizzazione delle informazioni utente con password in chiaro
 * ATTENZIONE: Usare solo per debug/admin, mai esporre in produzione
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informazioni dettagliate utente con credenziali (solo per admin)")
public class UserDetailsDto {

    @Schema(description = "ID dell'utente", example = "123")
    private Long id;

    @Schema(description = "Nome utente", example = "mario.rossi")
    private String username;

    @Schema(description = "Email dell'utente", example = "mario.rossi@company.com")
    private String email;

    @Schema(description = "Nome dell'utente", example = "Mario")
    private String firstName;

    @Schema(description = "Cognome dell'utente", example = "Rossi")
    private String lastName;

    @Schema(description = "Ruolo assegnato", example = "ADMIN")
    private String role;

    @Schema(description = "Stato attivo", example = "true")
    private Boolean isActive;

    @Schema(description = "Hash password BCrypt", example = "$2a$10$...")
    private String passwordHash;

    @Schema(description = "Data ultimo login", example = "2024-08-07T10:30:00")
    private LocalDateTime lastLoginAt;

    @Schema(description = "Numero totale di login", example = "42")
    private Long loginCount;

    @Schema(description = "Data e ora di creazione", example = "2024-08-07T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e ora ultima modifica", example = "2024-08-07T10:30:00")
    private LocalDateTime updatedAt;
}
