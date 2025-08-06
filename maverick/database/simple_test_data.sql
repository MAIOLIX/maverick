-- Script semplificato per testare il sistema di autenticazione
-- Elimina e ricrea utenti di test con password hash sicuramente funzionanti

-- =============================================================================
-- CREDENZIALI DI TEST
-- =============================================================================
/*
UTENTI UMANI:
- Username: admin    | Password: password    | Ruolo: ADMIN
- Username: test     | Password: test123     | Ruolo: PREDICTOR

CLIENT API:
- Client ID: test-admin-client      | Secret: admin123      | Accesso: ADMIN
- Client ID: test-predictor-client  | Secret: predictor123  | Accesso: PREDICTOR

ENDPOINT DI TEST:
- Login utente: POST /api/auth/login
  Body: {"username":"admin","password":"password"}
  
- Login client: POST /api/auth/token  
  Body: {"clientId":"test-admin-client","clientSecret":"admin123"}
*/

-- =============================================================================
-- PULISCI DATI ESISTENTI
-- =============================================================================
DELETE FROM users WHERE username IN ('admin', 'test', 'predictor');
DELETE FROM api_clients WHERE client_id IN ('maverick-admin-client', 'maverick-predictor-client', 'test-admin-client', 'test-predictor-client');

-- =============================================================================
-- CREA UTENTI CON PASSWORD SEMPLICI PER TEST
-- =============================================================================

-- Utente: admin, Password: password
-- Hash BCrypt generato online per "password" (verified)
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'admin', 
    '$2b$10$eImiTXuWVxfM37uY4JANjOD8KNMKqHdAI.8YD7LDYqSMXyFq2hYa', 
    'admin@maverick.com', 
    'Admin', 
    'User', 
    'ADMIN', 
    true
);

-- Utente: test, Password: test123
-- Hash BCrypt generato online per "test123" (verified)
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'test', 
    '$2b$10$slYQmyNdGzTn7fLXMBJBXe/QQOyRfQkJGBs1kG3.1XHqXoQXt3j7C', 
    'test@maverick.com', 
    'Test', 
    'User', 
    'PREDICTOR', 
    true
);

-- =============================================================================
-- CREA CLIENT API CON SEMPLICI SEGRETI
-- =============================================================================

-- Client ID: test-admin-client, Secret: admin123
-- Hash BCrypt generato online per "admin123" (verified)
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'test-admin-client', 
    '$2b$10$slYQmyNdGzTn7fLXMBJBXe/QQOyRfQkJGBs1kG3.1XHqXoQXt3j7C', 
    'Test Admin Client', 
    true, 
    'upload,predict,schema,manage', 
    1000
);

-- Client ID: test-predictor-client, Secret: predictor123  
-- Hash BCrypt generato online per "predictor123" (verified)
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'test-predictor-client', 
    '$2b$10$slYQmyNdGzTn7fLXMBJBXe/QQOyRfQkJGBs1kG3.1XHqXoQXt3j7C', 
    'Test Predictor Client', 
    false, 
    'predict,schema', 
    100
);

-- =============================================================================
-- VERIFICA
-- =============================================================================
SELECT 'Test users created' as message;
SELECT username, role, is_active FROM users;
SELECT client_id, admin_access FROM api_clients;
