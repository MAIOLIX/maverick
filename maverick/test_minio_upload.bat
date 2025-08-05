@echo off
echo ===================================================
echo TEST UPLOAD MODELLO SU MINIO
echo ===================================================

REM Test 1: Verifica connessione
echo.
echo [TEST 1] Verifica connessione MinIO...
curl -X GET "http://localhost:8080/api/v1/minio/models/test"
echo.

REM Test 2: Upload modello di esempio
echo.
echo [TEST 2] Upload modello di esempio...
echo Creando file di test...
echo "Questo e un modello di test" > test_model.txt

curl -X POST "http://localhost:8080/api/v1/minio/models/upload" ^
     -F "file=@test_model.txt" ^
     -F "modelName=iris" ^
     -F "version=v1.0"

echo.
echo.

REM Test 3: Upload di una seconda versione
echo [TEST 3] Upload seconda versione...
curl -X POST "http://localhost:8080/api/v1/minio/models/upload" ^
     -F "file=@test_model.txt" ^
     -F "modelName=iris" ^
     -F "version=v1.1"

echo.
echo.

REM Test 4: Upload di un modello diverso
echo [TEST 4] Upload modello diverso...
curl -X POST "http://localhost:8080/api/v1/minio/models/upload" ^
     -F "file=@test_model.txt" ^
     -F "modelName=wine-classifier" ^
     -F "version=v1.0"

echo.
echo ===================================================
echo TESTING COMPLETATO
echo Controlla MinIO console su http://localhost:32769
echo ===================================================

REM Pulizia
del test_model.txt

pause
