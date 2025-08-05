@echo off
echo ===================================================
echo TEST MAVERICK CONTROLLER - LOAD/REMOVE/PREDICT
echo ===================================================

REM Prima carichiamo alcuni modelli con upload completo
echo.
echo [SETUP] Upload modelli di test...
echo "Questo e un modello ONNX di test" > test_iris.onnx
echo "Questo e un modello PMML di test" > test_wine.pmml

echo.
echo [UPLOAD 1] Modello Iris ONNX...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_iris.onnx" ^
     -F "modelName=iris-classifier" ^
     -F "version=v1.0" ^
     -F "type=ONNX" ^
     -F "description=Modello Iris per test load/predict"

echo.
echo.

echo [UPLOAD 2] Modello Wine PMML...
curl -X POST "http://localhost:8080/api/v1/maverick/upload" ^
     -F "file=@test_wine.pmml" ^
     -F "modelName=wine-quality" ^
     -F "version=v2.0" ^
     -F "type=PMML" ^
     -F "description=Modello Wine per test load/predict"

echo.
echo.

REM Test dei nuovi metodi
echo [TEST 1] Load modello iris in memoria...
curl -X POST "http://localhost:8080/api/v1/maverick/load" ^
     -d "modelName=iris-classifier" ^
     -d "version=v1.0"

echo.
echo.

echo [TEST 2] Load modello wine in memoria...
curl -X POST "http://localhost:8080/api/v1/maverick/load" ^
     -d "modelName=wine-quality" ^
     -d "version=v2.0"

echo.
echo.

echo [TEST 3] Predizione iris (dati JSON di esempio)...
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v1.0/iris-classifier" ^
     -H "Content-Type: application/json" ^
     -d "{\"sepal_length\": 5.1, \"sepal_width\": 3.5, \"petal_length\": 1.4, \"petal_width\": 0.2}"

echo.
echo.

echo [TEST 4] Predizione wine (dati JSON di esempio)...
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v2.0/wine-quality" ^
     -H "Content-Type: application/json" ^
     -d "{\"alcohol\": 12.5, \"acidity\": 0.5, \"residual_sugar\": 1.8}"

echo.
echo.

echo [TEST 5] Rimozione modello iris dalla memoria...
curl -X DELETE "http://localhost:8080/api/v1/maverick/remove" ^
     -d "modelName=iris-classifier" ^
     -d "version=v1.0"

echo.
echo.

echo [TEST 6] Tentativo predizione su modello rimosso (dovrebbe fallire)...
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v1.0/iris-classifier" ^
     -H "Content-Type: application/json" ^
     -d "{\"sepal_length\": 5.1, \"sepal_width\": 3.5, \"petal_length\": 1.4, \"petal_width\": 0.2}"

echo.
echo.

echo [TEST 7] Rimozione modello inesistente (dovrebbe restituire warning)...
curl -X DELETE "http://localhost:8080/api/v1/maverick/remove" ^
     -d "modelName=non-esistente" ^
     -d "version=v1.0"

echo.
echo.

echo [TEST 8] Load modello inesistente (dovrebbe fallire)...
curl -X POST "http://localhost:8080/api/v1/maverick/load" ^
     -d "modelName=modello-inesistente" ^
     -d "version=v1.0"

echo.
echo ===================================================
echo TESTING COMPLETATO
echo.
echo Controlla:
echo - Database: SELECT model_name, version, prediction_count FROM models;
echo - Swagger: http://localhost:8080/swagger-ui.html
echo - Log applicazione per dettagli caricamento/predizioni
echo ===================================================

REM Pulizia
del test_iris.onnx
del test_wine.pmml

pause
