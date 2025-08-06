package com.maiolix.maverick.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maiolix.maverick.dto.auth.ClientCredentialsRequestDto;
import com.maiolix.maverick.dto.auth.LoginRequestDto;
import com.maiolix.maverick.dto.auth.LoginResponseDto;
import com.maiolix.maverick.dto.auth.TokenResponseDto;
import com.maiolix.maverick.entity.ApiClientEntity;
import com.maiolix.maverick.entity.UserEntity;
import com.maiolix.maverick.security.JwtTokenUtil;
import com.maiolix.maverick.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller per l'autenticazione JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints per l'autenticazione degli utenti e dei client API")
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    @Operation(
        summary = "Login utente",
        description = "Autentica un utente con username e password e restituisce un token JWT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Login effettuato con successo",
                content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Credenziali non valide"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Richiesta non valida"
            )
        }
    )
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getUsername());

        Optional<UserEntity> userOpt = userService.authenticateUser(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            String token = jwtTokenUtil.createUserToken(user);
            
            LoginResponseDto response = LoginResponseDto.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getTokenRemainingTime(token) / 1000) // in secondi
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

            log.info("User '{}' logged in successfully with role '{}'", user.getUsername(), user.getRole());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/token")
    @Operation(
        summary = "Autenticazione client API",
        description = "Autentica un client API con client_id e client_secret e restituisce un token JWT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Autenticazione effettuata con successo",
                content = @Content(schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Credenziali client non valide"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Richiesta non valida"
            )
        }
    )
    public ResponseEntity<TokenResponseDto> clientCredentials(@Valid @RequestBody ClientCredentialsRequestDto request) {
        log.info("Client credentials grant attempt for client: {}", request.getClientId());

        Optional<ApiClientEntity> clientOpt = userService.authenticateClient(request.getClientId(), request.getClientSecret());

        if (clientOpt.isPresent()) {
            ApiClientEntity client = clientOpt.get();
            String token = jwtTokenUtil.createClientToken(client);
            
            TokenResponseDto response = TokenResponseDto.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenUtil.getTokenRemainingTime(token) / 1000) // in secondi
                    .scope(client.getAllowedScopes())
                    .build();

            log.info("Client '{}' authenticated successfully with scopes '{}'", client.getClientId(), client.getAllowedScopes());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Failed client authentication for client: {}", request.getClientId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Valida token JWT",
        description = "Verifica se un token JWT è valido e non scaduto",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token valido"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o scaduto"
            )
        }
    )
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtTokenUtil.validateToken(token)) {
                log.debug("Token validated successfully for user: {}", jwtTokenUtil.getUsernameFromToken(token));
                return ResponseEntity.ok().build();
            }
        }
        
        log.debug("Token validation failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    @Operation(
        summary = "Informazioni utente corrente",
        description = "Restituisce le informazioni dell'utente o client attualmente autenticato",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Informazioni utente recuperate con successo"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token non valido o mancante"
            )
        }
    )
    public ResponseEntity<Object> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                String userType = jwtTokenUtil.getUserType(token);
                
                if ("HUMAN".equals(userType)) {
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    String role = userService.getUserRole(userId);
                    String email = jwtTokenUtil.getEmailFromToken(token);
                    
                    return ResponseEntity.ok(new UserInfoResponse(username, email, role, userType));
                } else if ("MACHINE".equals(userType)) {
                    Long clientId = jwtTokenUtil.getClientIdFromToken(token);
                    String role = userService.getClientRole(clientId);
                    
                    return ResponseEntity.ok(new ClientInfoResponse(username, role, userType));
                }
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Inner classes per le risposte
    private record UserInfoResponse(String username, String email, String role, String userType) {}
    private record ClientInfoResponse(String clientId, String role, String userType) {}
}
