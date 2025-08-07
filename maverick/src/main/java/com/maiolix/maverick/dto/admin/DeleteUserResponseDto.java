package com.maiolix.maverick.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di eliminazione utente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta per l'eliminazione di un utente")
public class DeleteUserResponseDto {

    @Schema(description = "ID dell'utente eliminato", example = "123")
    private Long id;

    @Schema(description = "Username dell'utente eliminato", example = "mario.rossi")
    private String username;

    @Schema(description = "Successo dell'operazione", example = "true")
    private Boolean success;

    @Schema(description = "Messaggio di conferma", example = "Utente eliminato con successo")
    private String message;
}
