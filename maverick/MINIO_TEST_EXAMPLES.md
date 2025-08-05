# 🚀 **Esempi di Test per MinIO Repository - Maverick ML**

## 📋 **Prerequisiti**
- Applicazione Maverick in esecuzione su `http://localhost:8080`
- MinIO server attivo su `localhost:32768`
- Bucket `maverick` creato automaticamente

---

## 🔧 **1. Test Connessione MinIO**

### **Health Check MinIO**
```bash
curl -X GET "http://localhost:8080/api/v1/minio/health"
```

**Risposta attesa:**
```json
{
  "status": "OK",
  "message": "MinIO connesso con successo",
  "endpoint": "http://localhost:32768",
  "defaultBucket": "maverick"
}
```

### **Lista Bucket Disponibili**
```bash
curl -X GET "http://localhost:8080/api/v1/minio/buckets"
```

---

## 📤 **2. Upload Modelli ML**

### **Upload Modello ONNX (con file)**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=iris-classifier" \
  -F "version=1.0" \
  -F "file=@iris.onnx"
```

### **Upload in Bucket Specifico**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=fraud-detector" \
  -F "version=2.1" \
  -F "file=@fraud_model.onnx" \
  -F "bucketName=production-models"
```

### **Upload Modello PMML**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=recommendation-engine" \
  -F "version=1.5" \
  -F "file=@recommendation.pmml"
```

**Risposta attesa:**
```json
{
  "status": "SUCCESS",
  "message": "Modello caricato con successo",
  "modelName": "iris-classifier",
  "version": "1.0",
  "fileName": "iris.onnx",
  "objectPath": "models/iris-classifier/1.0/iris.onnx",
  "bucket": "maverick",
  "size": 2048000
}
```

### **Struttura risultante in MinIO:**
```
maverick/
└── models/
    ├── iris-classifier/
    │   └── 1.0/
    │       └── iris.onnx
    ├── fraud-detector/
    │   └── 2.1/
    │       └── fraud_model.onnx
    └── recommendation-engine/
        └── 1.5/
            └── recommendation.pmml
```

---

## 📥 **3. Download e Verifica Modelli**

### **Download Modello Specifico**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/download/iris-classifier/1.0/iris.onnx"
```

### **Informazioni Dettagliate Modello**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/info/iris-classifier/1.0/iris.onnx"
```

**Risposta attesa:**
```json
{
  "status": "SUCCESS",
  "modelName": "iris-classifier",
  "version": "1.0",
  "fileName": "iris.onnx",
  "size": 2048000,
  "contentType": "application/octet-stream",
  "lastModified": "2025-08-05T10:30:00Z",
  "etag": "abc123def456...",
  "objectPath": "models/iris-classifier/1.0/iris.onnx"
}
```

### **Dimensione Totale Versione Modello**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/size/iris-classifier/1.0"
```

**Risposta attesa:**
```json
{
  "status": "SUCCESS",
  "modelName": "iris-classifier",
  "version": "1.0",
  "totalSizeBytes": 2048000,
  "totalSizeMB": 1.95
}
```

---

## 📋 **4. Gestione Versioni**

### **Lista Tutte le Versioni di un Modello**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/versions/iris-classifier"
```

### **Copia Modello tra Versioni**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=iris-classifier" \
  -d "sourceVersion=1.0" \
  -d "targetVersion=1.1" \
  -d "fileName=iris.onnx"
```

**Risposta attesa:**
```json
{
  "status": "SUCCESS",
  "message": "Modello copiato con successo",
  "modelName": "iris-classifier",
  "sourceVersion": "1.0",
  "targetVersion": "1.1",
  "fileName": "iris.onnx"
}
```

---

## 🗑️ **5. Cancellazione Modelli**

### **Cancella Modello Specifico**
```bash
curl -X DELETE "http://localhost:8080/api/v1/models/minio/iris-classifier/1.0/iris.onnx"
```

### **Cancella con Bucket Specifico**
```bash
curl -X DELETE "http://localhost:8080/api/v1/models/minio/fraud-detector/2.1/fraud_model.onnx?bucketName=production-models"
```

**Risposta attesa:**
```json
{
  "status": "SUCCESS",
  "message": "Modello cancellato con successo",
  "modelName": "iris-classifier",
  "version": "1.0",
  "fileName": "iris.onnx"
}
```

---

## 🧪 **6. Test Completo - Workflow ML**

### **Step 1: Upload Modello Iniziale**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=customer-segmentation" \
  -F "version=1.0" \
  -F "file=@customer_seg_v1.onnx"
```

### **Step 2: Verifica Upload**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/info/customer-segmentation/1.0/customer_seg_v1.onnx"
```

### **Step 3: Crea Nuova Versione**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=customer-segmentation" \
  -d "sourceVersion=1.0" \
  -d "targetVersion=1.1" \
  -d "fileName=customer_seg_v1.onnx"
```

### **Step 4: Verifica Dimensioni**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/size/customer-segmentation/1.1"
```

### **Step 5: Lista Versioni**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/versions/customer-segmentation"
```

---

## 🎯 **7. Test con Curl Avanzati**

### **Upload con Metadati Personalizzati**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "modelName=advanced-nlp" \
  -F "version=2.3" \
  -F "file=@nlp_model.onnx;type=application/octet-stream" \
  -v
```

### **Test Upload di File Grandi (con progress)**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=large-transformer" \
  -F "version=1.0" \
  -F "file=@large_model.onnx" \
  --progress-bar \
  -o upload_result.json
```

---

## 🔍 **8. Test di Errore e Edge Cases**

### **Upload senza File**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=test-error" \
  -F "version=1.0"
```

### **Download File Inesistente**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/download/non-existent/1.0/model.onnx"
```

### **Info Modello Inesistente**
```bash
curl -X GET "http://localhost:8080/api/v1/models/minio/info/non-existent/1.0/model.onnx"
```

---

## 📊 **9. Test Repository Diretto (se esponi endpoint)**

### **Test Connessione MinIO Base**
```bash
curl -X GET "http://localhost:8080/api/v1/minio/test-upload?fileName=test.txt&content=Hello MinIO"
```

### **Test Download Base**
```bash
curl -X GET "http://localhost:8080/api/v1/minio/test-download/test.txt"
```

---

## 🐍 **10. Test con Python**

```python
import requests
import json

# Test upload
def test_upload_model():
    url = "http://localhost:8080/api/v1/models/minio/upload"
    
    files = {
        'file': open('iris.onnx', 'rb')
    }
    
    data = {
        'modelName': 'iris-classifier',
        'version': '1.0'
    }
    
    response = requests.post(url, files=files, data=data)
    print(json.dumps(response.json(), indent=2))

# Test info
def test_model_info():
    url = "http://localhost:8080/api/v1/models/minio/info/iris-classifier/1.0/iris.onnx"
    response = requests.get(url)
    print(json.dumps(response.json(), indent=2))

# Esegui test
test_upload_model()
test_model_info()
```

---

## 🧪 **11. Test con Postman Collection**

```json
{
  "info": {
    "name": "Maverick MinIO Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Upload Model",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "formdata",
          "formdata": [
            {
              "key": "modelName",
              "value": "iris-classifier",
              "type": "text"
            },
            {
              "key": "version",
              "value": "1.0",
              "type": "text"
            },
            {
              "key": "file",
              "type": "file",
              "src": "iris.onnx"
            }
          ]
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/models/minio/upload",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "models", "minio", "upload"]
        }
      }
    }
  ]
}
```

---

## ✅ **12. Checklist Test Completo**

- [ ] ✅ **Connessione MinIO** - Health check passa
- [ ] ✅ **Upload Modello** - File caricato con successo
- [ ] ✅ **Struttura Path** - `models/{name}/{version}/` creata
- [ ] ✅ **Download Modello** - File scaricabile
- [ ] ✅ **Info Modello** - Metadati recuperati
- [ ] ✅ **Copia Versione** - Duplicazione funziona
- [ ] ✅ **Lista Versioni** - Tutte le versioni visibili
- [ ] ✅ **Calcolo Dimensioni** - Size calcolata correttamente
- [ ] ✅ **Cancellazione** - File rimosso con successo
- [ ] ✅ **Gestione Errori** - Errori gestiti appropriatamente

---

## 🚨 **Comandi di Troubleshooting**

### **Verifica Bucket MinIO**
```bash
# Accedi alla console MinIO
open http://localhost:32769
# Login: maiolix / Alessandro12
```

### **Log Applicazione**
```bash
# Controlla i log dell'app per errori MinIO
tail -f maverick.log | grep -i minio
```

### **Test Connessione Raw**
```bash
# Test diretto MinIO con mc client
mc config host add local http://localhost:32768 maiolix Alessandro12
mc ls local/maverick/models/
```

Questi esempi coprono tutti i casi d'uso principali del tuo repository MinIO per modelli ML! 🎉
