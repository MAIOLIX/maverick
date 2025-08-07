package com.maiolix.maverick.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di eliminazione client API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Risposta per l'eliminazione di un client API")
public class DeleteApiClientResponseDto {

    @Schema(description = "ID del client eliminato", example = "456")
    private Long id;

    @Schema(description = "Client ID eliminato", example = "mobile-app-prod")
    private String clientId;

    @Schema(description = "Successo dell'operazione", example = "true")
    private Boolean success;

    @Schema(description = "Messaggio di conferma", example = "Client API eliminato con successo")
    private String message;
}
