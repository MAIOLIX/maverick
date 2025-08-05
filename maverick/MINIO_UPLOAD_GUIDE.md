# MinIO Model Upload - Guida Rapida

## Overview
Sistema semplificato per il caricamento di modelli ML su MinIO con struttura automatica `modello/versione/`.

## Struttura Creata Automaticamente
```
bucket (maverick)/
├── iris/
│   ├── v1.0/
│   │   ├── model.pkl
│   │   └── metadata.json
│   └── v1.1/
│       └── model.pkl
└── wine-classifier/
    └── v1.0/
        └── model.onnx
```

## Endpoints Disponibili

### 1. Test Connessione
```bash
GET http://localhost:8080/api/v1/minio/models/test
```

**Risposta:**
```json
{
    "status": "SUCCESS",
    "message": "Connessione MinIO OK",
    "endpoint": "http://localhost:32768",
    "bucket": "maverick"
}
```

### 2. Upload Modello
```bash
POST http://localhost:8080/api/v1/minio/models/upload
```

**Parametri:**
- `file` (MultipartFile): File del modello
- `modelName` (String): Nome del modello
- `version` (String): Versione del modello

**Esempio cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/minio/models/upload" \
     -F "file=@iris_model.pkl" \
     -F "modelName=iris" \
     -F "version=v1.0"
```

**Risposta di successo:**
```json
{
    "status": "SUCCESS",
    "message": "Modello caricato con successo",
    "modelName": "iris",
    "version": "v1.0",
    "fileName": "iris_model.pkl",
    "path": "iris/v1.0/iris_model.pkl",
    "size": 1024,
    "bucket": "maverick"
}
```

## Configurazione MinIO
Configurazione in `application.properties`:
```properties
maverick.storage.minio.endpoint=http://localhost:32768
maverick.storage.minio.access-key=maiolix
maverick.storage.minio.secret-key=Alessandro12
maverick.storage.minio.default-bucket=maverick
```

## Test Rapido
1. Avvia l'applicazione: `mvn spring-boot:run`
2. Esegui: `test_minio_upload.bat`
3. Verifica su MinIO Console: http://localhost:32769

## Funzionalità Chiave
- ✅ **Creazione automatica cartelle**: MinIO crea automaticamente la struttura `modello/versione/` 
- ✅ **Bucket automatico**: Il bucket viene creato se non esiste
- ✅ **Validazione input**: Controlli sui parametri obbligatori
- ✅ **Logging dettagliato**: Monitoraggio completo delle operazioni
- ✅ **Gestione errori**: Messaggi di errore chiari e informativi

## Log di Esempio
```
🔧 Inizializzando MinIO client - Endpoint: http://localhost:32768
📁 Bucket 'maverick' creato
✅ MinIO client inizializzato con successo - Bucket: maverick
📤 Caricamento modello: iris/v1.0 -> iris/v1.0/model.pkl
✅ Modello caricato con successo: iris/v1.0/model.pkl
```

## Note Importanti
- MinIO crea automaticamente la struttura di cartelle tramite il path dell'oggetto
- Non è necessario creare esplicitamente le cartelle `modello` e `versione`
- Ogni upload sovrascrive file esistenti con lo stesso path
- Il sistema è ottimizzato per semplicità e velocità
