-- Script per inizializzare dati di test per il sistema di autenticazione Maverick
-- Eseguire dopo aver creato le tabelle del database

-- =============================================================================
-- Creazione tabelle (se non esistono già)
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'PREDICTOR')),
    is_active BOOLEAN DEFAULT true,
    user_type VARCHAR(20) DEFAULT 'HUMAN',
    last_login TIMESTAMP,
    login_count BIGINT DEFAULT 0,
    keycloak_subject VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_clients (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(100) UNIQUE NOT NULL,
    client_secret_hash VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    admin_access BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    allowed_scopes VARCHAR(500),
    rate_limit_per_minute INTEGER DEFAULT 1000,
    description TEXT,
    last_used_at TIMESTAMP,
    usage_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- Dati di test
-- =============================================================================

-- Utente admin (password: admin123)
-- Hash BCrypt per "admin123" con costo 12
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'admin', 
    '$2a$12$6b.QqwqDm5QhBxPO6YQyaOkNe.JbGjL4tQlDKl1fOgJFGW8VU8yLS', 
    'admin@maverick.com', 
    'Admin', 
    'User', 
    'ADMIN', 
    true
) ON CONFLICT (username) DO NOTHING;

-- Utente predictor (password: predictor123)
-- Hash BCrypt per "predictor123" con costo 12
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'predictor', 
    '$2a$12$HvqS4U3YJJe9W9LXHx5kou8oN2Xb1H5kWmKmWm6YzQj3PO4W6N8aS', 
    'predictor@maverick.com', 
    'Predictor', 
    'User', 
    'PREDICTOR', 
    true
) ON CONFLICT (username) DO NOTHING;

-- Client API admin (client_secret: admin-secret-2024)
-- Hash BCrypt per "admin-secret-2024" con costo 12
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'maverick-admin-client', 
    '$2a$12$8O9IJw4H5yF6ZLqJoQ3rKOzKzJz8Vz5Y3nJ7q5A2H9L4fK2S1D6Bp', 
    'Maverick Admin Client', 
    true, 
    'upload,predict,schema,manage', 
    1000
) ON CONFLICT (client_id) DO NOTHING;

-- Client API predictor (client_secret: predictor-secret-2024)
-- Hash BCrypt per "predictor-secret-2024" con costo 12
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'maverick-predictor-client', 
    '$2a$12$7N8HJv3G4xE5YKpIoP2qJNyJyJy7Uy4X2mI6p4A1G8K3eJ1R0C5Ao', 
    'Maverick Predictor Client', 
    false, 
    'predict,schema', 
    100
) ON CONFLICT (client_id) DO NOTHING;

-- =============================================================================
-- Verifica dati inseriti
-- =============================================================================

SELECT 'USERS' as table_name, count(*) as records FROM users
UNION ALL
SELECT 'API_CLIENTS' as table_name, count(*) as records FROM api_clients;

-- Mostra utenti creati
SELECT username, email, role, is_active, created_at FROM users ORDER BY created_at;

-- Mostra client creati
SELECT client_id, client_name, admin_access, allowed_scopes, rate_limit_per_minute, created_at FROM api_clients ORDER BY created_at;
