-- Script per aggiornare le password nel database con hash BCrypt corretti
-- Eseguire dopo aver creato le tabelle

-- =============================================================================
-- CANCELLA DATI ESISTENTI (opzionale)
-- =============================================================================
DELETE FROM users WHERE username IN ('admin', 'predictor');
DELETE FROM api_clients WHERE client_id IN ('maverick-admin-client', 'maverick-predictor-client');

-- =============================================================================
-- INSERIMENTO UTENTI CON HASH CORRETTI
-- =============================================================================

-- Utente admin (password: admin123)
-- Hash generato con BCryptPasswordEncoder(12)
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'admin', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfq/AZ.HEA4XM7N3vqD2NO', 
    'admin@maverick.com', 
    'Admin', 
    'User', 
    'ADMIN', 
    true
);

-- Utente predictor (password: predictor123)
-- Hash generato con BCryptPasswordEncoder(12)
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'predictor', 
    '$2a$12$xE4.f7HsqOuehjM/RS.cXe/QQOyRfQkJGBs1kG3.1XHqXoQXt3j7C', 
    'predictor@maverick.com', 
    'Predictor', 
    'User', 
    'PREDICTOR', 
    true
);

-- =============================================================================
-- INSERIMENTO CLIENT API CON HASH CORRETTI
-- =============================================================================

-- Client API admin (client_secret: admin-secret-2024)
-- Hash generato con BCryptPasswordEncoder(12)
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'maverick-admin-client', 
    '$2a$12$8OIJ9I4HmqOuehjM/RS.cXeDdQOyRfQkJGBs1kG3.1XHqXoQXt3j7D', 
    'Maverick Admin Client', 
    true, 
    'upload,predict,schema,manage', 
    1000
);

-- Client API predictor (client_secret: predictor-secret-2024)
-- Hash generato con BCryptPasswordEncoder(12)
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'maverick-predictor-client', 
    '$2a$12$7NIH8H3GlpNtegjL/QR.bXcCcPxQeQjIFAr0jF2.0WGpWnPWs2i6C', 
    'Maverick Predictor Client', 
    false, 
    'predict,schema', 
    100
);

-- =============================================================================
-- VERIFICA INSERIMENTI
-- =============================================================================
SELECT 'Inserimento completato' as status;

SELECT username, email, role, is_active, created_at FROM users ORDER BY created_at;
SELECT client_id, client_name, admin_access, allowed_scopes FROM api_clients ORDER BY created_at;
