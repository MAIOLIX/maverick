# Endpoint per Schema e Informazioni Modelli

## 📊 Nuovi Endpoint Aggiunti

### 1. **Schema di Input Specifico**
```http
GET /api/v1/maverick/models/{modelName}/versions/{version}/input-schema
```

**Descrizione**: Restituisce informazioni dettagliate sui parametri di input richiesti da un modello specifico.

**Parametri**:
- `modelName` (path) - Nome del modello
- `version` (path) - Versione del modello

**Risposta**:
```json
{
  "status": "SUCCESS",
  "message": "Schema di input recuperato con successo",
  "inputSchema": {
    "modelType": "ONNX",
    "totalInputs": 1,
    "inputNames": ["features"],
    "features": {
      "feature1": {"type": "numeric", "description": "First feature"},
      "feature2": {"type": "categorical", "values": ["A", "B", "C"]}
    },
    "inputExample": {
      "features": [1.0, 2.0, 3.0, 4.0]
    }
  },
  "timestamp": "2025-01-06T..."
}
```

### 2. **Informazioni Complete Modello**
```http
GET /api/v1/maverick/models/{modelName}/versions/{version}/info
```

**Descrizione**: Restituisce metadati completi, schema di input, informazioni di output e esempi di utilizzo.

**Parametri**:
- `modelName` (path) - Nome del modello
- `version` (path) - Versione del modello

**Risposta**:
```json
{
  "status": "SUCCESS",
  "message": "Informazioni modello recuperate con successo",
  "modelInfo": {
    "modelName": "iris-classifier",
    "version": "v1.0",
    "type": "ONNX",
    "hasLabelMapping": true,
    "labelMapping": {"0": "setosa", "1": "versicolor", "2": "virginica"},
    "inputSchema": {
      "modelType": "ONNX",
      "totalInputs": 1,
      "expectedFeatures": 4,
      "description": "float32 tensor with shape [1, 4]"
    },
    "databaseInfo": {
      "uploadedAt": "2025-01-06T...",
      "filePath": "iris-classifier/v1.0/model.onnx",
      "fileSize": 2048576,
      "isActive": true,
      "description": "Iris classification model"
    }
  },
  "storageProvider": "MinIO",
  "timestamp": "2025-01-06T..."
}
```

### 3. **Schema di Tutti i Modelli**
```http
GET /api/v1/maverick/models/schemas
```

**Descrizione**: Restituisce una panoramica degli schemi di input/output di tutti i modelli caricati in memoria.

**Risposta**:
```json
{
  "status": "SUCCESS",
  "message": "Schema di tutti i modelli recuperati con successo",
  "totalModels": 3,
  "modelsSchemas": {
    "iris-classifier_v1.0": {
      "modelName": "iris-classifier",
      "version": "v1.0",
      "type": "ONNX",
      "hasLabelMapping": true,
      "inputSchema": {
        "modelType": "ONNX",
        "totalInputs": 1,
        "inputNames": ["features"],
        "expectedFeatures": 4,
        "supervised": true,
        "outputClasses": 3,
        "labelMapping": {"0": "setosa", "1": "versicolor", "2": "virginica"}
      }
    },
    "fraud-detector_v2.1": {
      "modelName": "fraud-detector",
      "version": "v2.1",
      "type": "MOJO",
      "hasLabelMapping": false,
      "inputSchema": {
        "modelType": "MOJO",
        "totalFeatures": 15,
        "featureNames": ["amount", "time", "v1", "v2", ...],
        "supervised": true,
        "outputClasses": 2
      }
    }
  },
  "storageProvider": "MinIO",
  "timestamp": "2025-01-06T..."
}
```

## 🎯 Casi d'Uso

### Per Sviluppatori
- **Integrazione API**: Ottenere la struttura dei dati richiesti prima di chiamare `/predict`
- **Documentazione**: Generare automaticamente documentazione delle API ML
- **Validazione**: Verificare che i dati di input siano nel formato corretto

### Per Data Scientists
- **Esplorazione**: Comprendere la struttura dei modelli caricati
- **Debug**: Identificare problemi di formato nei dati di input
- **Monitoring**: Verificare che i modelli siano correttamente caricati con le feature attese

### Per DevOps
- **Health Check**: Monitorare lo stato di tutti i modelli
- **Deployment**: Verificare che i modelli siano attivi e accessibili
- **Troubleshooting**: Diagnosticare problemi di caricamento modelli

## 🔍 Esempi di Utilizzo

### Controllo Schema Prima della Predizione
```bash
# 1. Ottenere schema di input
curl -X GET "http://localhost:8080/api/v1/maverick/models/iris-classifier/versions/v1.0/input-schema"

# 2. Utilizzare le informazioni per formattare correttamente la richiesta di predizione
curl -X POST "http://localhost:8080/api/v1/maverick/predict/v1.0/iris-classifier" \
  -H "Content-Type: application/json" \
  -d '{"feature1": 5.1, "feature2": 3.5, "feature3": 1.4, "feature4": 0.2}'
```

### Panoramica di Tutti i Modelli
```bash
# Ottenere schema di tutti i modelli per documentazione
curl -X GET "http://localhost:8080/api/v1/maverick/models/schemas"
```

### Informazioni Complete per Debugging
```bash
# Ottenere tutte le informazioni disponibili su un modello
curl -X GET "http://localhost:8080/api/v1/maverick/models/iris-classifier/versions/v1.0/info"
```

## 🚀 Benefici

1. **Self-Documentation**: I modelli si auto-documentano esponendo la loro struttura
2. **Error Prevention**: Prevenzione di errori di formato nei dati di input
3. **Faster Integration**: Integrazione più rapida con applicazioni client
4. **Better Debugging**: Informazioni dettagliate per il debug e troubleshooting
5. **Monitoring**: Visibilità completa sullo stato e struttura dei modelli

## 🔧 Implementazione Tecnica

- **Astrazione**: Tutti i model handler implementano `getInputSchema()`
- **Formato Standard**: Ogni tipo di modello (ONNX, MOJO, PMML) fornisce uno schema strutturato
- **Integrazione Database**: Le informazioni di memoria sono integrate con i metadati del database
- **Error Handling**: Gestione robusta degli errori con messaggi informativi
- **Performance**: Operazioni read-only che non impattano le prestazioni di predizione
