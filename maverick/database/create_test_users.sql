-- Script per creare utenti di test con password hash corretti
-- Usare password semplici per il test iniziale

\echo 'Creando utenti di test...'

-- Elimina utenti esistenti
DELETE FROM users WHERE username IN ('admin', 'test');
DELETE FROM api_clients WHERE client_id IN ('test-client');

-- Crea utente admin con password 'password'
-- Hash BCrypt generato online per 'password' con rounds 10
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'admin', 
    '$2b$10$eImiTXuWVxfM37uY4JANjOD8KNPMKqHdAI.8YD7LDYqSMXyFq2hYa', 
    'admin@maverick.com', 
    'Admin', 
    'User', 
    'ADMIN', 
    true
);

-- Crea utente test con password 'test123'
-- Hash BCrypt generato online per 'test123' con rounds 10  
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active) 
VALUES (
    'test', 
    '$2b$10$SfLyJH6QG6HSKDjF5lI34OD8QKWZd4k0QQKJvzJnP8pCGZBqVtqhW', 
    'test@maverick.com', 
    'Test', 
    'User', 
    'PREDICTOR', 
    true
);

-- Crea client API con secret 'secret123'
-- Hash BCrypt generato online per 'secret123' con rounds 10
INSERT INTO api_clients (client_id, client_secret_hash, client_name, admin_access, allowed_scopes, rate_limit_per_minute) 
VALUES (
    'test-client', 
    '$2b$10$N9qo34c8kW5uH8XVj9zXXuyH0n7ScVJKJVAKF3lJ9uYWKJQXmQ9TK', 
    'Test Client', 
    true, 
    'upload,predict,schema,manage', 
    1000
);

SELECT 'Users created:' as message;
SELECT username, email, role FROM users;
SELECT client_id, client_name, admin_access FROM api_clients;
