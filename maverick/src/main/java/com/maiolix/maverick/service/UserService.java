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
     * Verifica se esiste già un utente con questo username
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveTrue(username).isPresent();
    }

    /**
     * Verifica se esiste già un client con questo clientId
     */
    public boolean existsByClientId(String clientId) {
        return apiClientRepository.findByClientIdAndIsActiveTrue(clientId).isPresent();
    }

    /**
     * Crea un nuovo utente con validazione
     */
    public UserEntity createUser(String username, String email, String password, UserEntity.Role role, String firstName, String lastName) {
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Username già esistente: " + username);
        }
        
        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
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
     * Crea un nuovo client API con validazione
     */
    public ApiClientEntity createApiClient(String clientId, String clientSecret, String name, String description, boolean adminAccess, Integer rateLimitPerMinute) {
        if (existsByClientId(clientId)) {
            throw new IllegalArgumentException("Client ID già esistente: " + clientId);
        }
        
        // Calcola rate limit
        int defaultRateLimit = adminAccess ? 1000 : 100;
        int finalRateLimit = rateLimitPerMinute != null ? rateLimitPerMinute : defaultRateLimit;
        
        ApiClientEntity client = ApiClientEntity.builder()
                .clientId(clientId)
                .clientSecretHash(passwordEncoder.encode(clientSecret))
                .name(name)
                .description(description)
                .adminAccess(adminAccess)
                .allowedScopes(adminAccess ? "upload,predict,schema,manage" : "predict,schema")
                .rateLimitPerMinute(finalRateLimit)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        ApiClientEntity saved = apiClientRepository.save(client);
        log.info("Created new API client '{}' with admin access: {}", clientId, adminAccess);
        return saved;
    }

    /**
     * Elimina utente per ID
     */
    public boolean deleteUser(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            userRepository.delete(user);
            log.info("Deleted user '{}' (ID: {})", user.getUsername(), userId);
            return true;
        } else {
            log.warn("Attempted to delete non-existent user with ID: {}", userId);
            return false;
        }
    }

    /**
     * Elimina client API per ID
     */
    public boolean deleteApiClient(Long clientId) {
        Optional<ApiClientEntity> clientOpt = apiClientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            ApiClientEntity client = clientOpt.get();
            apiClientRepository.delete(client);
            log.info("Deleted API client '{}' (ID: {})", client.getClientId(), clientId);
            return true;
        } else {
            log.warn("Attempted to delete non-existent API client with ID: {}", clientId);
            return false;
        }
    }

    /**
     * Ottiene tutti gli utenti (per admin)
     */
    public java.util.List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Ottiene tutti i client API (per admin)
     */
    public java.util.List<ApiClientEntity> getAllApiClients() {
        return apiClientRepository.findAll();
    }

    /**
     * Trova utente per ID
     */
    public Optional<UserEntity> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Trova client API per ID
     */
    public Optional<ApiClientEntity> findApiClientById(Long clientId) {
        return apiClientRepository.findById(clientId);
    }
}
