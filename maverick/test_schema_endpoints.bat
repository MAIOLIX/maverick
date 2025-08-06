@echo off
REM Script di test per i nuovi endpoint di schema dei modelli
REM Assicurati che l'applicazione Maverick sia in esecuzione su localhost:8080

set BASE_URL=http://localhost:8080/api/v1/maverick
set MODEL_NAME=iris-classifier
set VERSION=v1.0

echo 🚀 Test degli Endpoint di Schema Modelli
echo ========================================
echo.

echo 📋 1. Test Schema Input Specifico
echo GET %BASE_URL%/models/%MODEL_NAME%/versions/%VERSION%/input-schema
echo.
curl -s -X GET "%BASE_URL%/models/%MODEL_NAME%/versions/%VERSION%/input-schema"
echo.
echo.

echo ℹ️ 2. Test Informazioni Complete Modello
echo GET %BASE_URL%/models/%MODEL_NAME%/versions/%VERSION%/info
echo.
curl -s -X GET "%BASE_URL%/models/%MODEL_NAME%/versions/%VERSION%/info"
echo.
echo.

echo 📊 3. Test Schema di Tutti i Modelli
echo GET %BASE_URL%/models/schemas
echo.
curl -s -X GET "%BASE_URL%/models/schemas"
echo.
echo.

echo 🔍 4. Test Modello Non Esistente (404 atteso)
echo GET %BASE_URL%/models/non-esistente/versions/v1.0/input-schema
echo.
curl -s -X GET "%BASE_URL%/models/non-esistente/versions/v1.0/input-schema"
echo.
echo.

echo ✅ Test completati!
echo.
echo 💡 Suggerimenti:
echo    - Assicurati che l'applicazione sia in esecuzione
echo    - Verifica che ci siano modelli caricati in memoria
echo    - Usa 'GET /api/v1/maverick/models-in-memory' per vedere i modelli disponibili

pause
