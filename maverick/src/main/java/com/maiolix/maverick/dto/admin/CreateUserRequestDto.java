package com.maiolix.maverick.dto.admin;

import com.maiolix.maverick.entity.UserEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la richiesta di creazione di un nuovo utente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Richiesta per la creazione di un nuovo utente")
public class CreateUserRequestDto {

    @NotBlank(message = "Username è obbligatorio")
    @Size(min = 3, max = 50, message = "Username deve essere tra 3 e 50 caratteri")
    @Schema(description = "Nome utente univoco", example = "johndoe")
    private String username;

    @Email(message = "Email deve essere valida")
    @NotBlank(message = "Email è obbligatoria")
    @Schema(description = "Indirizzo email dell'utente", example = "john.doe@company.com")
    private String email;

    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 8, max = 100, message = "Password deve essere tra 8 e 100 caratteri")
    @Schema(description = "Password dell'utente (minimo 8 caratteri)", example = "SecurePassword123!")
    private String password;

    @Schema(description = "Nome dell'utente", example = "John")
    private String firstName;

    @Schema(description = "Cognome dell'utente", example = "Doe")
    private String lastName;

    @NotNull(message = "Ruolo è obbligatorio")
    @Schema(description = "Ruolo dell'utente nel sistema", 
            example = "ADMIN", 
            allowableValues = {"ADMIN", "PREDICTOR"})
    private UserEntity.Role role;
}
