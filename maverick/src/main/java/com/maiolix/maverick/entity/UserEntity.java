package com.maiolix.maverick.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità per gestire gli utenti umani (login con username/password)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "user_type")
    @Builder.Default
    private String userType = "HUMAN";

    @Column(name = "last_login")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count")
    @Builder.Default
    private Long loginCount = 0L;

    @Column(name = "keycloak_subject")
    private String keycloakSubject; // Per futura migrazione Keycloak

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Ruoli disponibili nel sistema
     */
    public enum Role {
        ADMIN,      // Accesso completo: upload, load, delete, monitoring
        PREDICTOR   // Solo predizioni: predict + input-schema
    }

    /**
     * Incrementa il contatore di login e aggiorna timestamp
     */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount != null ? this.loginCount : 0L) + 1;
    }

    /**
     * Verifica se l'utente è admin
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    /**
     * Verifica se l'utente può fare predizioni
     */
    public boolean canPredict() {
        return Role.ADMIN.equals(this.role) || Role.PREDICTOR.equals(this.role);
    }
}
