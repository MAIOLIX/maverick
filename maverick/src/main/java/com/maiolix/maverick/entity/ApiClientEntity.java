package com.maiolix.maverick.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entità per gestire i client API (machine-to-machine authentication)
 */
@Entity
@Table(name = "api_clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ApiClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false)
    private String clientSecretHash;

    @Column(name = "client_name", nullable = false)
    private String name;

    @Column(name = "admin_access")
    @Builder.Default
    private Boolean adminAccess = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "allowed_scopes")
    private String allowedScopes;

    @Column(name = "rate_limit_per_minute")
    @Builder.Default
    private Integer rateLimitPerMinute = 1000;

    @Column(name = "description")
    private String description;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Aggiorna l'ultimo utilizzo del client
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount = (this.usageCount != null ? this.usageCount : 0L) + 1;
    }

    /**
     * Verifica se il client ha uno scope specifico
     */
    public boolean hasScope(String scope) {
        return allowedScopes != null && allowedScopes.contains(scope);
    }

    /**
     * Verifica se il client è admin
     */
    public boolean isAdmin() {
        return Boolean.TRUE.equals(this.adminAccess);
    }

    /**
     * Verifica se il client ha accesso admin
     */
    public boolean isAdminAccess() {
        return Boolean.TRUE.equals(this.adminAccess);
    }

    /**
     * Verifica se il client può fare predizioni
     */
    public boolean canPredict() {
        return true; // Tutti i client possono fare predizioni
    }
}
