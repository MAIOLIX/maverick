package com.maiolix.maverick.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maiolix.maverick.dto.admin.ApiClientDetailsDto;
import com.maiolix.maverick.dto.admin.CreateApiClientRequestDto;
import com.maiolix.maverick.dto.admin.CreateApiClientResponseDto;
import com.maiolix.maverick.dto.admin.CreateUserRequestDto;
import com.maiolix.maverick.dto.admin.CreateUserResponseDto;
import com.maiolix.maverick.dto.admin.DeleteApiClientResponseDto;
import com.maiolix.maverick.dto.admin.DeleteUserResponseDto;
import com.maiolix.maverick.dto.admin.UserDetailsDto;
import com.maiolix.maverick.entity.ApiClientEntity;
import com.maiolix.maverick.entity.UserEntity;
import com.maiolix.maverick.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller per la gestione amministrativa di utenti e client API
 * Accessibile solo ad utenti con ruolo ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "Endpoints per la gestione amministrativa di utenti e client API")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crea nuovo utente",
        description = "Crea un nuovo utente nel sistema. Accessibile solo agli amministratori.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Utente creato con successo",
                content = @Content(schema = @Schema(implementation = CreateUserResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dati di richiesta non validi o username già esistente"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<CreateUserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        log.info("Admin request to create user: {}", request.getUsername());

        try {
            UserEntity user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getFirstName(),
                request.getLastName()
            );

            CreateUserResponseDto response = CreateUserResponseDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
                    .isActive(user.getIsActive())
                    .createdAt(user.getCreatedAt())
                    .message("Utente creato con successo")
                    .build();

            log.info("User '{}' created successfully with role '{}'", user.getUsername(), user.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to create user '{}': {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                CreateUserResponseDto.builder()
                    .message("Errore nella creazione utente: " + e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/api-clients")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crea nuovo client API",
        description = "Crea un nuovo client API nel sistema. Accessibile solo agli amministratori.",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Client API creato con successo",
                content = @Content(schema = @Schema(implementation = CreateApiClientResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Dati di richiesta non validi o client ID già esistente"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<CreateApiClientResponseDto> createApiClient(@Valid @RequestBody CreateApiClientRequestDto request) {
        log.info("Admin request to create API client: {}", request.getClientId());

        try {
            ApiClientEntity client = userService.createApiClient(
                request.getClientId(),
                request.getClientSecret(),
                request.getName(),
                request.getDescription(),
                request.getAdminAccess(),
                request.getRateLimitPerMinute()
            );

            CreateApiClientResponseDto response = CreateApiClientResponseDto.builder()
                    .id(client.getId())
                    .clientId(client.getClientId())
                    .name(client.getName())
                    .description(client.getDescription())
                    .adminAccess(client.getAdminAccess())
                    .allowedScopes(client.getAllowedScopes())
                    .rateLimitPerMinute(client.getRateLimitPerMinute())
                    .isActive(client.getIsActive())
                    .createdAt(client.getCreatedAt())
                    .clientSecret(request.getClientSecret()) // Mostrato solo alla creazione
                    .message("Client API creato con successo")
                    .build();

            log.info("API Client '{}' created successfully with admin access: {}", 
                client.getClientId(), client.getAdminAccess());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to create API client '{}': {}", request.getClientId(), e.getMessage());
            return ResponseEntity.badRequest().body(
                CreateApiClientResponseDto.builder()
                    .message("Errore nella creazione client API: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Lista tutti gli utenti",
        description = "Recupera la lista completa di tutti gli utenti con hash password. SOLO PER DEBUG/ADMIN!",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista utenti recuperata con successo"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<List<UserDetailsDto>> getAllUsers() {
        log.info("Admin request to list all users");

        List<UserEntity> users = userService.getAllUsers();
        List<UserDetailsDto> response = users.stream()
            .map(user -> UserDetailsDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .passwordHash(user.getPasswordHash())
                .lastLoginAt(user.getLastLoginAt())
                .loginCount(user.getLoginCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build())
            .toList();

        log.info("Retrieved {} users for admin", users.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api-clients")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Lista tutti i client API",
        description = "Recupera la lista completa di tutti i client API con hash secret. SOLO PER DEBUG/ADMIN!",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista client API recuperata con successo"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<List<ApiClientDetailsDto>> getAllApiClients() {
        log.info("Admin request to list all API clients");

        List<ApiClientEntity> clients = userService.getAllApiClients();
        List<ApiClientDetailsDto> response = clients.stream()
            .map(client -> ApiClientDetailsDto.builder()
                .id(client.getId())
                .clientId(client.getClientId())
                .name(client.getName())
                .description(client.getDescription())
                .adminAccess(client.getAdminAccess())
                .isActive(client.getIsActive())
                .allowedScopes(client.getAllowedScopes())
                .rateLimitPerMinute(client.getRateLimitPerMinute())
                .clientSecretHash(client.getClientSecretHash())
                .lastUsedAt(client.getLastUsedAt())
                .usageCount(client.getUsageCount())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build())
            .toList();

        log.info("Retrieved {} API clients for admin", clients.size());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Elimina utente",
        description = "Elimina un utente dal sistema. Accessibile solo agli amministratori.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Utente eliminato con successo",
                content = @Content(schema = @Schema(implementation = DeleteUserResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Utente non trovato"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<DeleteUserResponseDto> deleteUser(@PathVariable Long userId) {
        log.info("Admin request to delete user with ID: {}", userId);

        // Prima recupera le informazioni utente per il log
        Optional<UserEntity> userOpt = userService.findUserById(userId);
        if (userOpt.isEmpty()) {
            log.warn("Attempted to delete non-existent user with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                DeleteUserResponseDto.builder()
                    .id(userId)
                    .success(false)
                    .message("Utente non trovato")
                    .build()
            );
        }

        UserEntity user = userOpt.get();
        boolean deleted = userService.deleteUser(userId);

        if (deleted) {
            DeleteUserResponseDto response = DeleteUserResponseDto.builder()
                    .id(userId)
                    .username(user.getUsername())
                    .success(true)
                    .message("Utente eliminato con successo")
                    .build();

            log.info("User '{}' (ID: {}) deleted successfully by admin", user.getUsername(), userId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DeleteUserResponseDto.builder()
                    .id(userId)
                    .username(user.getUsername())
                    .success(false)
                    .message("Errore durante l'eliminazione dell'utente")
                    .build()
            );
        }
    }

    @DeleteMapping("/api-clients/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Elimina client API",
        description = "Elimina un client API dal sistema. Accessibile solo agli amministratori.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Client API eliminato con successo",
                content = @Content(schema = @Schema(implementation = DeleteApiClientResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Client API non trovato"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Accesso negato - solo amministratori"
            )
        }
    )
    public ResponseEntity<DeleteApiClientResponseDto> deleteApiClient(@PathVariable Long clientId) {
        log.info("Admin request to delete API client with ID: {}", clientId);

        // Prima recupera le informazioni client per il log
        Optional<ApiClientEntity> clientOpt = userService.findApiClientById(clientId);
        if (clientOpt.isEmpty()) {
            log.warn("Attempted to delete non-existent API client with ID: {}", clientId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                DeleteApiClientResponseDto.builder()
                    .id(clientId)
                    .success(false)
                    .message("Client API non trovato")
                    .build()
            );
        }

        ApiClientEntity client = clientOpt.get();
        boolean deleted = userService.deleteApiClient(clientId);

        if (deleted) {
            DeleteApiClientResponseDto response = DeleteApiClientResponseDto.builder()
                    .id(clientId)
                    .clientId(client.getClientId())
                    .success(true)
                    .message("Client API eliminato con successo")
                    .build();

            log.info("API Client '{}' (ID: {}) deleted successfully by admin", client.getClientId(), clientId);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DeleteApiClientResponseDto.builder()
                    .id(clientId)
                    .clientId(client.getClientId())
                    .success(false)
                    .message("Errore durante l'eliminazione del client API")
                    .build()
            );
        }
    }
}
