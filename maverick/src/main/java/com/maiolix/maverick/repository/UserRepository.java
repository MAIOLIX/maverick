package com.maiolix.maverick.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maiolix.maverick.entity.UserEntity;

/**
 * Repository per la gestione degli utenti
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Trova utente per username
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Trova utente per username e che sia attivo
     */
    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);

    /**
     * Ottiene solo il ruolo di un utente come stringa (per performance)
     */
    @Query("SELECT CAST(u.role AS string) FROM UserEntity u WHERE u.id = :userId AND u.isActive = true")
    String findRoleByUserId(@Param("userId") Long userId);

    /**
     * Verifica se un utente esiste ed è attivo
     */
    boolean existsByIdAndIsActiveTrue(Long userId);

    /**
     * Verifica se un utente è attivo
     */
    @Query("SELECT u.isActive FROM UserEntity u WHERE u.id = :userId")
    Boolean isUserActive(@Param("userId") Long userId);

    /**
     * Trova utente per Keycloak subject (per futura migrazione)
     */
    Optional<UserEntity> findByKeycloakSubject(String keycloakSubject);

    /**
     * Verifica se username esiste
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se email esiste
     */
    boolean existsByEmail(String email);
}
