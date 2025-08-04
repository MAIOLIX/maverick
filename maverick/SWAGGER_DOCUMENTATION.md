# Swagger/OpenAPI Documentation

## Accesso alla documentazione API

Una volta avviata l'applicazione Maverick, la documentazione Swagger sarà disponibile ai seguenti URL:

### Swagger UI (Interfaccia Interattiva)
```
http://localhost:8080/swagger-ui.html
```

### Documentazione JSON
```
http://localhost:8080/api-docs
```

## Caratteristiche della documentazione

### 🔍 **Esplorazione API**
- **Interfaccia interattiva**: Esplora tutti gli endpoint disponibili
- **Schemi di dati**: Visualizza i modelli di input/output
- **Esempi**: Ogni endpoint include esempi pratici di utilizzo

### 🧪 **Test delle API**
- **Try it out**: Testa direttamente gli endpoint dall'interfaccia
- **Parametri pre-compilati**: Esempi di valori per ogni parametro
- **Risposte in tempo reale**: Visualizza le risposte del server

### 📖 **Documentazione completa**
- **Descrizioni dettagliate**: Ogni endpoint è completamente documentato
- **Codici di stato HTTP**: Spiegazione di tutti i possibili codici di risposta
- **Formati supportati**: MOJO, ONNX, PMML

## Endpoints principali

### 📤 **Upload Modello**
- **Endpoint**: `POST /api/v1/models/upload`
- **Descrizione**: Carica un nuovo modello ML
- **Formati supportati**: `.mojo`, `.onnx`, `.pmml`

### 🔮 **Predizione**
- **Endpoint**: `POST /api/v1/models/predict/{version}/{modelName}`
- **Descrizione**: Esegue predizioni su dati di input

### ℹ️ **Informazioni Modello**
- **Endpoint**: `GET /api/v1/models/info/{version}/{modelName}`
- **Descrizione**: Restituisce metadati del modello

### 📋 **Schema Input**
- **Endpoint**: `GET /api/v1/models/schema/{version}/{modelName}`
- **Descrizione**: Ottiene lo schema dei dati di input richiesti

### 📝 **Gestione Cache**
- **Lista modelli**: `GET /api/v1/models/list`
- **Aggiungi alla cache**: `POST /api/v1/models/add`
- **Rimuovi dalla cache**: `DELETE /api/v1/models/{version}/{modelName}`

## Esempi di utilizzo

### Upload di un modello ONNX
```bash
curl -X POST "http://localhost:8080/api/v1/models/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@iris-model.onnx" \
  -F "modelName=iris-classifier" \
  -F "type=ONNX" \
  -F "version=1.0"
```

### Predizione
```bash
curl -X POST "http://localhost:8080/api/v1/models/predict/1.0/iris-classifier" \
  -H "Content-Type: application/json" \
  -d '{
    "sepal_length": 5.1,
    "sepal_width": 3.5,
    "petal_length": 1.4,
    "petal_width": 0.2
  }'
```

## Personalizzazione

La configurazione Swagger può essere modificata nel file:
- `src/main/java/com/maiolix/maverick/config/SwaggerConfig.java`

Le impostazioni UI sono configurabili in:
- `src/main/resources/application.properties`

## Sicurezza

⚠️ **Nota di sicurezza**: In ambiente di produzione, considera di:
- Disabilitare Swagger UI (`springdoc.swagger-ui.enabled=false`)
- Limitare l'accesso tramite autenticazione
- Esporre solo la documentazione JSON per integrazioni
