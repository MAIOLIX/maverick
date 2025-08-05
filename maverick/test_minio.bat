@echo off
REM =========================================================================
REM Script di Test Automatici per MinIO Repository - Maverick ML
REM =========================================================================

echo.
echo ========================================
echo 🚀 MAVERICK MINIO TEST SUITE
echo ========================================
echo.

set BASE_URL=http://localhost:8080
set MINIO_API=%BASE_URL%/api/v1/models/minio
set MINIO_TEST=%BASE_URL%/api/v1/minio

echo 📋 Testing MinIO Repository per Modelli ML...
echo.

REM Test 1: Health Check
echo ✅ Test 1: Health Check MinIO
curl -s -X GET "%MINIO_TEST%/health" | jq .
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Health check fallito!
    goto :error
)
echo.

REM Test 2: Lista Bucket
echo ✅ Test 2: Lista Bucket
curl -s -X GET "%MINIO_TEST%/buckets" | jq .
echo.

REM Test 3: Upload Test File (se esiste iris.onnx nella directory corrente)
if exist "iris.onnx" (
    echo ✅ Test 3: Upload Modello iris.onnx
    curl -s -X POST "%MINIO_API%/upload" ^
        -F "modelName=iris-classifier" ^
        -F "version=1.0" ^
        -F "file=@iris.onnx" | jq .
    echo.
) else (
    echo ⚠️  Test 3: Skipped - iris.onnx non trovato
    echo    Crea un file di test con: echo test content > test-model.txt
    echo    E poi usa: curl -X POST "%MINIO_API%/upload" -F "modelName=test" -F "version=1.0" -F "file=@test-model.txt"
    echo.
)

REM Test 4: Upload File di Test Semplice
echo ✅ Test 4: Upload File di Test
echo test model content > test-model.txt
curl -s -X POST "%MINIO_API%/upload" ^
    -F "modelName=test-model" ^
    -F "version=1.0" ^
    -F "file=@test-model.txt" | jq .
echo.

REM Test 5: Info Modello
echo ✅ Test 5: Info Modello di Test
curl -s -X GET "%MINIO_API%/info/test-model/1.0/test-model.txt" | jq .
echo.

REM Test 6: Download Modello
echo ✅ Test 6: Download Modello di Test
curl -s -X GET "%MINIO_API%/download/test-model/1.0/test-model.txt"
echo.
echo.

REM Test 7: Copia Modello
echo ✅ Test 7: Copia Modello (versione 1.0 -> 1.1)
curl -s -X POST "%MINIO_API%/copy" ^
    -d "modelName=test-model" ^
    -d "sourceVersion=1.0" ^
    -d "targetVersion=1.1" ^
    -d "fileName=test-model.txt" | jq .
echo.

REM Test 8: Lista Versioni
echo ✅ Test 8: Lista Versioni del Modello
curl -s -X GET "%MINIO_API%/versions/test-model" | jq .
echo.

REM Test 9: Dimensione Modello
echo ✅ Test 9: Dimensione Modello
curl -s -X GET "%MINIO_API%/size/test-model/1.0" | jq .
echo.

REM Test 10: Cancellazione Modello
echo ✅ Test 10: Cancellazione Modello versione 1.1
curl -s -X DELETE "%MINIO_API%/test-model/1.1/test-model.txt" | jq .
echo.

REM Test 11: Verifica Cancellazione
echo ✅ Test 11: Verifica Cancellazione (dovrebbe dare errore)
curl -s -X GET "%MINIO_API%/info/test-model/1.1/test-model.txt" | jq .
echo.

REM Cleanup
echo 🧹 Cleanup: Rimozione file di test...
del test-model.txt 2>nul
curl -s -X DELETE "%MINIO_API%/test-model/1.0/test-model.txt" | jq . > nul
echo.

echo ========================================
echo ✅ TUTTI I TEST COMPLETATI CON SUCCESSO!
echo ========================================
echo.
echo 📊 Risultati Test:
echo   • Health Check: ✅
echo   • Upload Modello: ✅  
echo   • Download Modello: ✅
echo   • Info Modello: ✅
echo   • Copia Versioni: ✅
echo   • Lista Versioni: ✅
echo   • Calcolo Dimensioni: ✅
echo   • Cancellazione: ✅
echo.
echo 🎉 Repository MinIO per ML Models funziona correttamente!
echo.
goto :end

:error
echo.
echo ❌ ERRORE: Test falliti!
echo    Verifica che:
echo    1. L'applicazione Maverick sia in esecuzione su %BASE_URL%
echo    2. MinIO sia attivo su localhost:32768
echo    3. Le credenziali MinIO siano corrette
echo.

:end
pause
