@echo off
echo ===================================================
echo TEST MAVERICK CONTROLLER - UPLOAD COMPLETO
echo ===================================================

REM Crea file di test per diversi tipi di modello
echo.
echo [SETUP] Creazione file di test...
echo "Questo e un modello ONNX di test" > test_model.onnx
echo "Questo e un modello PMML di test" > test_model.pmml
echo "Questo e un modello MOJO di test" > test_model.zip

echo.
echo [TEST 1] Upload modello ONNX...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_model.onnx" ^
     -F "modelName=iris-classifier" ^
     -F "version=v1.0" ^
     -F "type=ONNX" ^
     -F "description=Modello Iris con ONNX Runtime"

echo.
echo.

echo [TEST 2] Upload modello PMML...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_model.pmml" ^
     -F "modelName=wine-quality" ^
     -F "version=v2.1" ^
     -F "type=PMML" ^
     -F "description=Modello qualita vino in PMML"

echo.
echo.

echo [TEST 3] Upload modello MOJO...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_model.zip" ^
     -F "modelName=titanic-survival" ^
     -F "version=v1.5" ^
     -F "type=MOJO" ^
     -F "description=Predizione sopravvivenza Titanic con H2O"

echo.
echo.

echo [TEST 4] Test errore - modello duplicato...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_model.onnx" ^
     -F "modelName=iris-classifier" ^
     -F "version=v1.0" ^
     -F "type=ONNX" ^
     -F "description=Tentativo duplicato"

echo.
echo.

echo [TEST 5] Test errore - tipo modello non valido...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_model.onnx" ^
     -F "modelName=test-invalid" ^
     -F "version=v1.0" ^
     -F "type=INVALID_TYPE" ^
     -F "description=Test tipo non valido"

echo.
echo ===================================================
echo TESTING COMPLETATO
echo.
echo Controlla:
echo - Database PostgreSQL: SELECT * FROM models;
echo - MinIO Console: http://localhost:32769
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo ===================================================

REM Pulizia
del test_model.onnx
del test_model.pmml
del test_model.zip

pause
