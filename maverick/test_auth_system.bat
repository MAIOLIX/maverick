@echo off
echo =============================================================================
echo Test del Sistema di Autenticazione Maverick
echo =============================================================================
echo.
echo CREDENZIALI DI TEST:
echo - Utente admin: username=admin, password=password
echo - Utente test: username=test, password=test123  
echo - Client admin: clientId=test-admin-client, secret=admin123
echo - Client predictor: clientId=test-predictor-client, secret=predictor123
echo.
echo =============================================================================

echo.
echo 1. Test Login Utente ADMIN
echo ---------------------
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password\"}"

echo.
echo.
echo 2. Test Login Utente PREDICTOR
echo ------------------------------
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test\",\"password\":\"test123\"}"

echo.
echo.
echo 3. Test Autenticazione Client API ADMIN
echo ----------------------------------------
curl -X POST http://localhost:8080/api/auth/token ^
  -H "Content-Type: application/json" ^
  -d "{\"clientId\":\"test-admin-client\",\"clientSecret\":\"admin123\"}"

echo.
echo.
echo 4. Test Autenticazione Client API PREDICTOR
echo --------------------------------------------
curl -X POST http://localhost:8080/api/auth/token ^
  -H "Content-Type: application/json" ^
  -d "{\"clientId\":\"test-predictor-client\",\"clientSecret\":\"predictor123\"}"

echo.
echo.
echo 3. Test Validazione Token (sostituire TOKEN con token reale)
echo -----------------------------------------------------------
echo curl -X POST http://localhost:8080/api/auth/validate ^
echo   -H "Authorization: Bearer TOKEN"

echo.
echo.
echo 4. Test Informazioni Utente Corrente (sostituire TOKEN con token reale)
echo -----------------------------------------------------------------------
echo curl -X GET http://localhost:8080/api/auth/me ^
echo   -H "Authorization: Bearer TOKEN"

echo.
echo.
echo 5. Test Accesso Protetto - Predizione (sostituire TOKEN con token reale)
echo ------------------------------------------------------------------------
echo curl -X POST http://localhost:8080/api/models/predict/model-name ^
echo   -H "Authorization: Bearer TOKEN" ^
echo   -H "Content-Type: application/json" ^
echo   -d "{\"data\":[]}"

echo.
echo.
echo 6. Test Accesso Protetto - Upload (solo ADMIN, sostituire TOKEN con token reale)
echo -------------------------------------------------------------------------------
echo curl -X POST http://localhost:8080/api/models/upload ^
echo   -H "Authorization: Bearer TOKEN" ^
echo   -F "file=@model.pkl"

echo.
echo ================================
echo Per testare, avviare il server con: mvn spring-boot:run
echo ================================
