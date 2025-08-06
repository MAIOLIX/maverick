@echo off
echo =============================================================================
echo Setup Database di Autenticazione Maverick
echo =============================================================================

echo.
echo Questo script:
echo 1. Crea le tabelle users e api_clients
echo 2. Assegna i permessi all'utente maverick
echo 3. Inserisce dati di test
echo.

set /p POSTGRES_HOST="Host PostgreSQL (default: localhost): " || set POSTGRES_HOST=localhost
set /p POSTGRES_PORT="Porta PostgreSQL (default: 5432): " || set POSTGRES_PORT=5432
set /p POSTGRES_DB="Nome Database (default: maverickDB): " || set POSTGRES_DB=maverickDB
set /p POSTGRES_USER="Utente Admin (default: postgres): " || set POSTGRES_USER=postgres

echo.
echo Esecuzione setup database...
echo Host: %POSTGRES_HOST%:%POSTGRES_PORT%
echo Database: %POSTGRES_DB%
echo User: %POSTGRES_USER%
echo.

psql -h %POSTGRES_HOST% -p %POSTGRES_PORT% -d %POSTGRES_DB% -U %POSTGRES_USER% -f database\setup_auth_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Setup completato con successo!
    echo.
    echo CREDENZIALI DI TEST:
    echo -------------------
    echo Utenti Umani:
    echo   - Username: admin     ^| Password: admin123     ^| Ruolo: ADMIN
    echo   - Username: predictor ^| Password: predictor123 ^| Ruolo: PREDICTOR
    echo.
    echo Client API:
    echo   - Client ID: maverick-admin-client     ^| Secret: admin-secret-2024     ^| Tipo: Admin
    echo   - Client ID: maverick-predictor-client ^| Secret: predictor-secret-2024 ^| Tipo: Predictor
    echo.
    echo 🚀 Ora puoi avviare l'applicazione con: mvn spring-boot:run
) else (
    echo.
    echo ❌ Errore durante il setup del database
    echo Verifica:
    echo 1. PostgreSQL è avviato
    echo 2. Le credenziali sono corrette
    echo 3. L'utente ha i permessi per creare tabelle
)

pause
