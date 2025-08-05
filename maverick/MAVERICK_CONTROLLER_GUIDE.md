# MaverickController - API Reference

## 🎯 **9 Endpoints Principali**

### 1. **Upload Modello**
```http
POST /api/v1/maverick/upload
Content-Type: multipart/form-data
```
Carica modello su MinIO e salva metadati nel database (stato: **inattivo**).

**Parametri:**
- `file` (MultipartFile) - File modello (max 100MB)
- `modelName` (String) - Nome modello
- `version` (String) - Versione (es: v1.0)
- `type` (String) - ONNX|PMML|MOJO|H2O
- `description` (String, opz) - Descrizione

### 2. **Attiva Modello**
```http
POST /api/v1/maverick/load?modelName=iris&version=v1.0
```
Carica modello da MinIO in memoria e lo attiva nel database.

### 3. **Predizione**
```http
POST /api/v1/maverick/predict?modelName=iris&version=v1.0
Content-Type: application/json

{
  "features": [5.1, 3.5, 1.4, 0.2]
}
```

### 4. **Disattiva Modello**
```http
DELETE /api/v1/maverick/remove?modelName=iris&version=v1.0
```
Rimuove da memoria e disattiva nel database (file su MinIO rimane).

### 5. **Elimina Modello**
```http
DELETE /api/v1/maverick/delete?modelName=iris&version=v1.0
```
Elimina da memoria, database E MinIO completamente.

### 6. **Lista Modelli in Memoria**
```http
GET /api/v1/maverick/models-in-memory
```

### 7. **Lista Modelli Database**
```http
GET /api/v1/maverick/models-database?page=0&size=10
```

### 8. **Ricarica Modelli Attivi**
```http
POST /api/v1/maverick/bootstrap/reload
```
Ricarica tutti i modelli attivi dal database in memoria.

### 9. **Audit Sistema**
```http
GET /api/v1/maverick/bootstrap/audit
```
Verifica consistenza tra database e memoria.

## 🔄 **Workflow Tipico**

```mermaid
graph LR
    A[Upload] --> B[Load] --> C[Predict] --> D[Remove] --> E[Delete]
    A --> |Salva su MinIO + DB| B
    B --> |Carica in memoria| C  
    C --> |Usa modello| D
    D --> |Disattiva| E
    E --> |Elimina tutto| F[Fine]
```

## 📊 **Stati Modello**

| Stato | Database | Memoria | MinIO | Azioni Possibili |
|-------|----------|---------|-------|------------------|
| **Uploaded** | ✅ (inattivo) | ❌ | ✅ | Load, Delete |
| **Active** | ✅ (attivo) | ✅ | ✅ | Predict, Remove, Delete |
| **Removed** | ✅ (inattivo) | ❌ | ✅ | Load, Delete |
| **Deleted** | ❌ | ❌ | ❌ | Upload nuovo |

## 🧪 **Test Rapidi**

```bash
# Upload
curl -X POST -F "file=@model.onnx" -F "modelName=test" -F "version=v1.0" -F "type=ONNX" \
  http://localhost:8080/api/v1/maverick/upload

# Load  
curl -X POST "http://localhost:8080/api/v1/maverick/load?modelName=test&version=v1.0"

# Predict
curl -X POST -H "Content-Type: application/json" \
  -d '{"features":[1,2,3,4]}' \
  "http://localhost:8080/api/v1/maverick/predict?modelName=test&version=v1.0"
```

### 2. Load Modello in Memoria
**POST** `/api/v1/maverick/load`

Carica un modello dal database nella cache in memoria per eseguire predizioni veloci.

**Parametri:**
- `modelName` (String): Nome del modello
- `version` (String): Versione del modello

**Esempio cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/maverick/load" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"
```

**Risposta:**
```json
{
    "status": "SUCCESS",
    "message": "Modello caricato e attivato con successo in memoria",
    "modelName": "iris-classifier",
    "version": "v1.0",
    "type": "ONNX",
    "fileSize": 1024,
    "minioPath": "iris-classifier/v1.0/iris_model.onnx",
    "loadedAt": 1691234567890,
    "cached": true,
    "isActive": true
}
```

### 3. Remove Modello dalla Memoria
**DELETE** `/api/v1/maverick/remove`

Rimuove un modello dalla cache in memoria e lo disattiva nel database.

**Parametri:**
- `modelName` (String): Nome del modello
- `version` (String): Versione del modello

**Esempio cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/v1/maverick/remove" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"
```

**Risposta:**
```json
{
    "status": "SUCCESS", 
    "message": "Modello rimosso dalla memoria e disattivato con successo",
    "modelName": "iris-classifier",
    "version": "v1.0",
    "removedAt": 1691234567890,
    "isActive": false
}
```

### 4. Predizione
**POST** `/api/v1/maverick/predict/{version}/{modelName}`

Esegue una predizione usando un modello caricato in memoria.

**Path Parameters:**
- `version` (String): Versione del modello
- `modelName` (String): Nome del modello

**Body:** JSON con i dati di input per la predizione

**Esempio cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v1.0/iris-classifier" \
     -H "Content-Type: application/json" \
     -d '{"sepal_length": 5.1, "sepal_width": 3.5, "petal_length": 1.4, "petal_width": 0.2}'
```

**Risposta:**
```json
{
    "status": "SUCCESS",
    "prediction": {
        "class": "setosa",
        "probability": 0.98
    },
    "modelName": "iris-classifier",
    "version": "v1.0",
    "executionTimeMs": 15,
    "timestamp": 1691234567890
}
```

### 5. LISTA MODELLI IN MEMORIA

#### Endpoint
```
GET /api/v1/maverick/models-in-memory
```

#### Descrizione
Restituisce la lista completa di tutti i modelli attualmente caricati nella cache in memoria con statistiche dettagliate.

#### Headers
```
Content-Type: application/json
```

#### Risposta di successo
```json
{
    "status": "SUCCESS",
    "message": "Lista modelli in memoria recuperata con successo",
    "models": [
        {
            "modelName": "iris_model",
            "version": "v1.0",
            "type": "ONNX",
            "key": "iris_model_v1.0",
            "hasHandler": true,
            "hasLabelMapping": true
        }
    ],
    "statistics": {
        "totalModels": 1,
        "modelTypes": ["ONNX"],
        "uniqueModelNames": 1
    },
    "timestamp": 1699123456789
}
```

#### Esempio cURL
```bash
curl -X GET "http://localhost:8080/api/v1/maverick/models-in-memory" 
     -H "Content-Type: application/json"
```

#### Configurazione Postman
1. **Method**: GET
2. **URL**: `http://localhost:8080/api/v1/maverick/models-in-memory`
3. **Headers**: 
   - Content-Type: application/json

### 6. LISTA MODELLI NEL DATABASE

#### Endpoint
```
GET /api/v1/maverick/models-database
```

#### Descrizione
Restituisce la lista completa di tutti i modelli salvati nel database PostgreSQL con paginazione e statistiche dettagliate.

#### Parametri Query
- `page` (int, opzionale): Numero pagina (0-based, default: 0)
- `size` (int, opzionale): Dimensione pagina (default: 20)

#### Headers
```
Content-Type: application/json
```

#### Risposta di successo
```json
{
    "status": "SUCCESS",
    "message": "Lista modelli nel database recuperata con successo",
    "models": [
        {
            "id": 1,
            "modelUuid": "550e8400-e29b-41d4-a716-446655440000",
            "modelName": "iris-classifier",
            "version": "v1.0",
            "type": "ONNX",
            "description": "Modello classificazione Iris",
            "filePath": "iris-classifier/v1.0/iris_model.onnx",
            "fileSize": 1024,
            "fileHash": "abc123",
            "storageType": "MINIO",
            "bucketName": "maverick",
            "isActive": true,
            "status": "READY",
            "predictionCount": 15,
            "lastUsedAt": "2025-08-05T15:30:00",
            "createdAt": "2025-08-05T10:30:00",
            "updatedAt": "2025-08-05T15:30:00"
        }
    ],
    "pagination": {
        "totalElements": 1,
        "totalPages": 1,
        "currentPage": 0,
        "pageSize": 20,
        "hasNext": false,
        "hasPrevious": false
    },
    "statistics": {
        "totalModels": 1,
        "totalPages": 1,
        "currentPage": 0,
        "pageSize": 20,
        "modelTypes": ["ONNX"],
        "activeModels": 1,
        "storageTypes": ["MINIO"]
    },
    "timestamp": 1699123456789
}
```

#### Esempio cURL
```bash
# Lista prima pagina (default)
curl -X GET "http://localhost:8080/api/v1/maverick/models-database" \
     -H "Content-Type: application/json"

# Lista con paginazione personalizzata
curl -X GET "http://localhost:8080/api/v1/maverick/models-database?page=0&size=10" \
     -H "Content-Type: application/json"
```

### 7. ELIMINAZIONE COMPLETA MODELLO

#### Endpoint
```
DELETE /api/v1/maverick/delete
```

#### Descrizione
Elimina completamente un modello da memoria, database PostgreSQL e storage MinIO in una singola operazione.

#### Parametri
- `modelName` (String): Nome del modello
- `version` (String): Versione del modello

#### Risposta di successo
```json
{
    "status": "SUCCESS",
    "message": "Eliminazione modello completata",
    "modelName": "iris-classifier",
    "version": "v1.0",
    "deletedAt": 1691234567890,
    "operations": {
        "memoryRemoved": true,
        "minioDeleted": true,
        "databaseDeleted": true
    }
}
```

#### Esempio cURL
```bash
curl -X DELETE "http://localhost:8080/api/v1/maverick/delete" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"
```

### 8. RICARICAMENTO MODELLI ATTIVI

#### Endpoint
```
POST /api/v1/maverick/bootstrap/reload
```

#### Descrizione
Ricarica manualmente tutti i modelli attivi dal database nella cache in memoria, pulendo prima la cache esistente.

#### Risposta di successo
```json
{
    "status": "SUCCESS",
    "message": "Ricaricamento modelli completato",
    "before": {
        "databaseActive": 3,
        "memoryCache": 2
    },
    "after": {
        "memoryCache": 3
    },
    "reloadedAt": 1691234567890
}
```

#### Esempio cURL
```bash
curl -X POST "http://localhost:8080/api/v1/maverick/bootstrap/reload"
```

### 9. AUDIT STATO MODELLI

#### Endpoint
```
GET /api/v1/maverick/bootstrap/audit
```

#### Descrizione
Verifica la coerenza tra modelli attivi nel database e modelli caricati in memoria, identificando eventuali discrepanze.

#### Risposta di successo
```json
{
    "status": "SUCCESS",
    "message": "Audit modelli completato",
    "statistics": {
        "databaseActiveModels": 3,
        "memoryCachedModels": 3,
        "isConsistent": true,
        "missingInCache": 0,
        "extraInCache": 0
    },
    "details": {
        "activeModelsInDb": ["iris-classifier_v1.0", "text-model_v2.0"],
        "cachedModelsInMemory": ["iris-classifier_v1.0", "text-model_v2.0"],
        "missingInCache": [],
        "extraInCache": []
    },
    "timestamp": 1691234567890
}
```

#### Esempio cURL
```bash
curl -X GET "http://localhost:8080/api/v1/maverick/bootstrap/audit"
```

---

## Workflow Completo

### 🚀 **CARICAMENTO AUTOMATICO ALL'AVVIO**
- **Bootstrap**: All'avvio dell'applicazione, tutti i modelli con `isActive: true` vengono automaticamente caricati in memoria
- **Gestione errori**: Se un modello non può essere caricato, viene automaticamente disattivato nel database
- **Log dettagliati**: Statistiche complete del caricamento con conteggi per tipo di modello

### ⚠️ Stato di Attivazione
- **Upload**: I modelli vengono caricati con stato `isActive: false`
- **Load**: Il comando load attiva automaticamente il modello (`isActive: true`)
- **Remove**: Il comando remove disattiva automaticamente il modello (`isActive: false`)
- **Bootstrap**: All'avvio vengono caricati automaticamente tutti i modelli attivi
- **Predict**: Solo i modelli attivi possono essere utilizzati per predizioni

### Ciclo di Vita del Modello
```
0. BOOTSTRAP  → Caricamento automatico modelli attivi all'avvio
1. UPLOAD     → Carica modello su MinIO + metadati DB (stato: NON ATTIVO)
2. LOAD       → Attiva modello e carica in memoria per predizioni
3. MONITOR    → Verifica modelli in memoria e database
4. PREDICT    → Esegue predizioni veloci (con tracking)
5. REMOVE     → Rimuove dalla memoria e disattiva nel database
6. DELETE     → Eliminazione completa da memoria, database e MinIO
7. AUDIT      → Verifica coerenza DB-Memoria
8. RELOAD     → Ricaricamento manuale di tutti i modelli attivi
```

### Esempio Workflow Completo
```bash
# 0. L'applicazione all'avvio carica automaticamente tutti i modelli attivi

# 1. Upload del modello
curl -X POST "http://localhost:8080/api/v1/maverick/upload" \
     -F "file=@iris_model.onnx" \
     -F "modelName=iris-classifier" \
     -F "version=v1.0" \
     -F "type=ONNX"

# 2. Carica in memoria 
curl -X POST "http://localhost:8080/api/v1/maverick/load" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"

# 3. Verifica modelli in memoria
curl -X GET "http://localhost:8080/api/v1/maverick/models-in-memory" \
     -H "Content-Type: application/json"

# 4. Verifica modelli nel database
curl -X GET "http://localhost:8080/api/v1/maverick/models-database" \
     -H "Content-Type: application/json"

# 5. Audit coerenza DB-Memoria
curl -X GET "http://localhost:8080/api/v1/maverick/bootstrap/audit"

# 6. Esegui predizioni
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v1.0/iris-classifier" \
     -H "Content-Type: application/json" \
     -d '{"sepal_length": 5.1, "sepal_width": 3.5, "petal_length": 1.4, "petal_width": 0.2}'

# 7. Ricaricamento manuale modelli (se necessario)
curl -X POST "http://localhost:8080/api/v1/maverick/bootstrap/reload"

# 8. Rimuovi dalla memoria e disattiva (temporaneo)
curl -X DELETE "http://localhost:8080/api/v1/maverick/remove" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"

# 9. Eliminazione completa (memoria + database + MinIO)
curl -X DELETE "http://localhost:8080/api/v1/maverick/delete" \
     -d "modelName=iris-classifier" \
     -d "version=v1.0"
```

## Operazioni Eseguite

### 1. Validazione Input
- ✅ **File richiesto**: Verifica che il file non sia vuoto
- ✅ **Nome modello**: Solo caratteri alfanumerici, underscore e trattini
- ✅ **Versione**: Formato v1.0 o 1.0.0
- ✅ **Tipo modello**: Uno dei tipi supportati
- ✅ **Dimensione file**: Massimo 100MB
- ✅ **Duplicati**: Verifica che modello+versione non esista già

### 2. Upload MinIO
- 📁 **Struttura automatica**: `modello/versione/file`
- 🔄 **Bucket automatico**: Crea bucket se non esiste
- 📤 **Stream efficiente**: Upload diretto senza salvataggio temporaneo

### 3. Salvataggio Database
- 💾 **Metadati completi**: Tutti i campi della tabella `models`
- 🔒 **Integrità**: Hash del file per verifica
- 🏷️ **UUID automatico**: Generazione automatica dell'identificatore univoco
- ⏰ **Audit trail**: Timestamp di creazione e aggiornamento

### 4. Caricamento in Memoria
- ⚡ **Cache modelli**: Caricamento in memoria per predizioni veloci
- 🔄 **Registry centralizato**: Gestione modelli tramite `ModelRegistry`
- 🎯 **Handler specifici**: Supporto ONNX, PMML, MOJO, H2O
- 📊 **Tracking utilizzo**: Conteggio predizioni automatico
- ✅ **Attivazione automatica**: I modelli vengono attivati al caricamento in memoria
- 🚀 **Bootstrap automatico**: All'avvio carica tutti i modelli attivi dal database

### 5. Predizioni Real-time
- 🚀 **Performance**: Predizioni da cache in memoria
- ⏱️ **Timing**: Monitoraggio tempo esecuzione
- 📈 **Statistiche**: Aggiornamento contatori utilizzo
- 🎯 **Path personalizzato**: `/predict/{version}/{modelName}`

## Gestione Errori

Il controller utilizza il `GlobalExceptionHandler` per gestire gli errori:

### ModelUploadException
```json
{
    "errorCode": "MODEL_UPLOAD_ERROR",
    "message": "Tipo modello non valido: INVALID",
    "path": "/api/v1/maverick/upload",
    "status": 400,
    "suggestion": "Check your model file format and parameters"
}
```

### Errori Comuni
- **File vuoto**: `File richiesto`
- **Nome non valido**: `Nome modello deve contenere solo lettere, numeri, underscore e trattini`
- **Versione non valida**: `Versione deve essere nel formato v1.0 o 1.0.0`
- **Tipo non supportato**: `Tipo modello non valido: X. Valori supportati: ONNX, PMML, MOJO, H2O, ONNX_EXT`
- **File troppo grande**: `File troppo grande. Massimo 100MB consentiti`
- **Duplicato**: `Modello X versione Y già esistente`

## Integrazione Database

### Tabella `models`
```sql
INSERT INTO models (
    model_name, version, type, description,
    storage_type, file_path, bucket_name,
    file_size, file_hash, content_type,
    status, is_active, created_by, updated_by
) VALUES (
    'iris-classifier', 'v1.0', 'ONNX', 'Modello classificazione Iris',
    'MINIO', 'iris-classifier/v1.0/iris_model.onnx', 'maverick',
    1024, 'abc123', 'application/octet-stream',
    'READY', true, 'api-user', 'api-user'
);
```

### Vantaggi Integrazione
- 🔍 **Ricerca avanzata**: Query complesse sui metadati
- 📊 **Analytics**: Statistiche di utilizzo e performance
- 🔐 **Sicurezza**: Controllo accessi a livello database
- 🔄 **Backup**: Backup automatico dei metadati
- ⚡ **Performance**: Query ottimizzate con indici

## Test Rapido

1. **Avvia applicazione:**
   ```bash
   mvn spring-boot:run
   ```

2. **Test upload completo:**
   ```bash
   test_maverick_upload.bat
   ```

3. **Test workflow completo:**
   ```bash
   test_maverick_complete.bat
   ```

4. **Verifica risultati:**
   - Database: `SELECT model_name, version, prediction_count, last_used_at FROM models;`
   - MinIO: http://localhost:32769
   - Swagger: http://localhost:8080/swagger-ui.html

## Monitoraggio

### Log di Esempio
```
🚀 Avvio caricamento automatico modelli attivi...
📋 Trovati 3 modelli attivi da caricare
✅ Modello caricato: iris-classifier v1.0
✅ Modello caricato: text-model v2.0
✅ Modello caricato: image-classifier v1.5
🎯 Caricamento completato - Successi: 3, Fallimenti: 0
📊 Modelli in memoria: 3 attivi
   📈 ONNX: 2 modelli
   📈 PMML: 1 modelli
🚀 Upload modello: new-model v1.0 tipo=ONNX
📤 Caricamento su MinIO: new-model/v1.0/new_model.onnx
💾 Salvataggio nel database...
✅ Upload completato: ID=4, UUID=550e8400-e29b-41d4-a716-446655440001
```

### Metriche
- **Tempo upload**: MinIO + Database
- **Dimensioni file**: Tracking per analytics
- **Errori**: Monitoraggio fallimenti
- **Usage**: Contatori per modelli più utilizzati
