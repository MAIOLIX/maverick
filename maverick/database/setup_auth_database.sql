-- Script per creare le tabelle del sistema di autenticazione e assegnare i permessi
-- Eseguire come superuser (postgres) o owner del database

-- =============================================================================
-- CREAZIONE TABELLE
-- =============================================================================

-- Tabella users
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

-- Tabella api_clients
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
-- ASSEGNAZIONE PERMESSI ALL'UTENTE MAVERICK
-- =============================================================================

-- Assegna tutti i permessi su users
GRANT ALL PRIVILEGES ON TABLE users TO maverick;
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO maverick;

-- Assegna tutti i permessi su api_clients
GRANT ALL PRIVILEGES ON TABLE api_clients TO maverick;
GRANT USAGE, SELECT ON SEQUENCE api_clients_id_seq TO maverick;

-- Assegna permessi generali al database (se necessario)
GRANT CONNECT ON DATABASE "maverickDB" TO maverick;
GRANT USAGE ON SCHEMA public TO maverick;
GRANT CREATE ON SCHEMA public TO maverick;

-- =============================================================================
-- VERIFICA PERMESSI
-- =============================================================================

-- Verifica permessi sulle tabelle
SELECT table_name, privilege_type 
FROM information_schema.role_table_grants 
WHERE grantee = 'maverick' 
AND table_name IN ('users', 'api_clients');

-- Verifica permessi sulle sequenze
SELECT object_name as sequence_name, privilege_type 
FROM information_schema.role_usage_grants 
WHERE grantee = 'maverick' 
AND object_name LIKE '%_seq';

-- =============================================================================
-- INSERIMENTO DATI DI TEST
-- =============================================================================

-- Utente admin (password: admin123)
-- Hash BCrypt per "admin123" con costo 12
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active, user_type, login_count, created_at, updated_at) 
VALUES (
    'admin', 
    '$2a$12$K8gOBY8BZKZmqkNH8dABN.M1UQGZ0eO6z/cKyLKyHoGqF6I4aXjqG', 
    'admin@maverick.com', 
    'Admin', 
    'User', 
    'ADMIN', 
    true,
    'HUMAN',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Utente predictor (password: predictor123)
-- Hash BCrypt per "predictor123" con costo 12
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active, user_type, login_count, created_at, updated_at) 
VALUES (
    'predictor', 
    '$2a$12$H7pS3R2YJJe8W8KXGx4koL7oN1Wb0G4kVmJmVm5YzPj2PO3V5M7aR', 
    'predictor@maverick.com', 
    'Predictor', 
    'User', 
    'PREDICTOR', 
    true,
    'HUMAN',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Client API admin (client_secret: admin-secret-2024)
-- Hash BCrypt per "admin-secret-2024" con costo 12
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute, usage_count, created_at, updated_at) 
VALUES (
    'maverick-admin-client', 
    '$2a$12$F5nJ2L1YIIe7W7JXFx3jnN6oM0Vb9F3kUmJmUm4YyOi1PN2U4L6aM', 
    'Maverick Admin Client', 
    true, 
    'upload,predict,schema,manage', 
    1000,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (client_id) DO NOTHING;

-- Client API predictor (client_secret: predictor-secret-2024)
-- Hash BCrypt per "predictor-secret-2024" con costo 12
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute, usage_count, created_at, updated_at) 
VALUES (
    'maverick-predictor-client', 
    '$2a$12$E4mI1K0YHHe6V6IWEx2imM5nL9Ua8E2jTlImTl3XxNh0OM1T3K5aL', 
    'Maverick Predictor Client', 
    false, 
    'predict,schema', 
    100,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (client_id) DO NOTHING;

-- =============================================================================
-- VERIFICA INSERIMENTI
-- =============================================================================

SELECT 'USERS' as table_name, count(*) as records FROM users
UNION ALL
SELECT 'API_CLIENTS' as table_name, count(*) as records FROM api_clients;

-- Mostra utenti creati
SELECT username, email, role, is_active, created_at FROM users ORDER BY created_at;

-- Mostra client creati
SELECT client_id, client_name, admin_access, allowed_scopes, rate_limit_per_minute, created_at FROM api_clients ORDER BY created_at;
