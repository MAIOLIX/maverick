package com.maiolix.maverick.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.maiolix.maverick.entity.ApiClientEntity;

/**
 * Repository per la gestione dei client API
 */
@Repository
public interface ApiClientRepository extends JpaRepository<ApiClientEntity, Long> {

    /**
     * Trova client per client_id
     */
    Optional<ApiClientEntity> findByClientId(String clientId);

    /**
     * Trova client per client_id e che sia attivo
     */
    Optional<ApiClientEntity> findByClientIdAndIsActiveTrue(String clientId);

    /**
     * Verifica se un client esiste ed è attivo
     */
    boolean existsByIdAndIsActiveTrue(Long clientId);

    /**
     * Trova tutti i client attivi
     */
    @Query("SELECT c FROM ApiClientEntity c WHERE c.isActive = true")
    java.util.List<ApiClientEntity> findAllActiveClients();
}
