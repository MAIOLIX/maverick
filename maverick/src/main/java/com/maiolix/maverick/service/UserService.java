package com.maiolix.maverick.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.maiolix.maverick.entity.ApiClientEntity;
import com.maiolix.maverick.entity.UserEntity;
import com.maiolix.maverick.repository.ApiClientRepository;
import com.maiolix.maverick.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service per la gestione degli utenti e autenticazione
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ApiClientRepository apiClientRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica utente umano con username e password
     */
    public Optional<UserEntity> authenticateUser(String username, String password) {
        Optional<UserEntity> userOpt = userRepository.findByUsernameAndIsActiveTrue(username);
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                // Aggiorna last login
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("User '{}' authenticated successfully", username);
                return Optional.of(user);
            } else {
                log.warn("Failed authentication attempt for user '{}'", username);
            }
        } else {
            log.warn("Authentication attempt for non-existent user '{}'", username);
        }
        
        return Optional.empty();
    }

    /**
     * Autentica client API con client_id e client_secret
     */
    public Optional<ApiClientEntity> authenticateClient(String clientId, String clientSecret) {
        Optional<ApiClientEntity> clientOpt = apiClientRepository.findByClientIdAndIsActiveTrue(clientId);
        
        if (clientOpt.isPresent()) {
            ApiClientEntity client = clientOpt.get();
            if (passwordEncoder.matches(clientSecret, client.getClientSecretHash())) {
                // Aggiorna last used
                client.setLastUsedAt(LocalDateTime.now());
                apiClientRepository.save(client);
                log.info("API Client '{}' authenticated successfully", clientId);
                return Optional.of(client);
            } else {
                log.warn("Failed authentication attempt for client '{}'", clientId);
            }
        } else {
            log.warn("Authentication attempt for non-existent client '{}'", clientId);
        }
        
        return Optional.empty();
    }

    /**
     * Ottiene il ruolo di un utente (con cache)
     */
    @Cacheable(value = "userRoles", key = "#userId")
    public String getUserRole(Long userId) {
        return userRepository.findRoleByUserId(userId);
    }

    /**
     * Ottiene il ruolo di un client API (con cache)
     */
    @Cacheable(value = "clientRoles", key = "#clientId")
    public String getClientRole(Long clientId) {
        Optional<ApiClientEntity> client = apiClientRepository.findById(clientId);
        return client.map(c -> c.isAdminAccess() ? "ADMIN" : "PREDICTOR").orElse(null);
    }

    /**
     * Trova utente per username
     */
    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username);
    }

    /**
     * Trova client per client_id
     */
    public Optional<ApiClientEntity> findByClientId(String clientId) {
        return apiClientRepository.findByClientIdAndIsActiveTrue(clientId);
    }

    /**
     * Verifica se un utente esiste ed è attivo
     */
    public boolean isUserActive(Long userId) {
        return userRepository.existsByIdAndIsActiveTrue(userId);
    }

    /**
     * Verifica se un client esiste ed è attivo
     */
    public boolean isClientActive(Long clientId) {
        return apiClientRepository.existsByIdAndIsActiveTrue(clientId);
    }

    /**
     * Crea un nuovo utente
     */
    public UserEntity createUser(String username, String email, String password, UserEntity.Role role) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        UserEntity saved = userRepository.save(user);
        log.info("Created new user '{}' with role '{}'", username, role);
        return saved;
    }

    /**
     * Crea un nuovo client API
     */
    public ApiClientEntity createApiClient(String clientId, String clientSecret, String name, boolean adminAccess) {
        ApiClientEntity client = ApiClientEntity.builder()
                .clientId(clientId)
                .clientSecretHash(passwordEncoder.encode(clientSecret))
                .name(name)
                .adminAccess(adminAccess)
                .allowedScopes(adminAccess ? "upload,predict,schema" : "predict,schema")
                .rateLimitPerMinute(adminAccess ? 1000 : 100)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        ApiClientEntity saved = apiClientRepository.save(client);
        log.info("Created new API client '{}' with admin access: {}", clientId, adminAccess);
        return saved;
    }
}
