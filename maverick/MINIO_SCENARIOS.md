# 🎯 **Scenari di Test Reali per Repository MinIO - Maverick ML**

## 🏗️ **Scenario 1: Deploy Nuovo Modello in Produzione**

### **Step 1: Upload Modello Iniziale**
```bash
# Upload modello fraud detection v1.0
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=fraud-detection" \
  -F "version=1.0" \
  -F "file=@fraud_model_v1.onnx"
```

### **Step 2: Verifica Integrità**
```bash
# Controlla info e dimensioni
curl "http://localhost:8080/api/v1/models/minio/info/fraud-detection/1.0/fraud_model_v1.onnx"
curl "http://localhost:8080/api/v1/models/minio/size/fraud-detection/1.0"
```

### **Step 3: Test Download**
```bash
# Verifica che il modello sia scaricabile
curl -o "downloaded_model.onnx" \
  "http://localhost:8080/api/v1/models/minio/download/fraud-detection/1.0/fraud_model_v1.onnx"
```

---

## 🔄 **Scenario 2: Aggiornamento Modello con Rollback**

### **Step 1: Deploy Nuova Versione**
```bash
# Upload versione 2.0 migliorata
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=fraud-detection" \
  -F "version=2.0" \
  -F "file=@fraud_model_v2.onnx"
```

### **Step 2: Backup Versione Precedente**
```bash
# Crea backup della v1.0 come v1.0-backup
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=fraud-detection" \
  -d "sourceVersion=1.0" \
  -d "targetVersion=1.0-backup" \
  -d "fileName=fraud_model_v1.onnx"
```

### **Step 3: Test della Nuova Versione**
```bash
# Verifica la nuova versione
curl "http://localhost:8080/api/v1/models/minio/info/fraud-detection/2.0/fraud_model_v2.onnx"
```

### **Step 4: Rollback (se necessario)**
```bash
# Se v2.0 ha problemi, copia il backup su v2.1
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=fraud-detection" \
  -d "sourceVersion=1.0-backup" \
  -d "targetVersion=2.1" \
  -d "fileName=fraud_model_v1.onnx"
```

---

## 🏭 **Scenario 3: Gestione Multi-Ambiente**

### **Upload in Ambiente di Sviluppo**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=recommendation-engine" \
  -F "version=dev-1.0" \
  -F "file=@recommendation_dev.onnx" \
  -F "bucketName=development-models"
```

### **Promozione a Staging**
```bash
# Copia da dev a staging bucket
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=recommendation-engine" \
  -d "sourceVersion=dev-1.0" \
  -d "targetVersion=staging-1.0" \
  -d "fileName=recommendation_dev.onnx"
```

### **Deploy in Produzione**
```bash
# Upload finale in produzione
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=recommendation-engine" \
  -F "version=prod-1.0" \
  -F "file=@recommendation_prod.onnx" \
  -F "bucketName=production-models"
```

---

## 📊 **Scenario 4: Monitoring e Analisi**

### **Analisi Utilizzo Storage**
```bash
# Lista tutti i modelli
curl "http://localhost:8080/api/v1/models/minio/versions/fraud-detection"

# Calcola dimensioni per ogni versione
curl "http://localhost:8080/api/v1/models/minio/size/fraud-detection/1.0"
curl "http://localhost:8080/api/v1/models/minio/size/fraud-detection/2.0"
curl "http://localhost:8080/api/v1/models/minio/size/fraud-detection/1.0-backup"
```

### **Audit Trail**
```bash
# Informazioni dettagliate per audit
curl "http://localhost:8080/api/v1/models/minio/info/fraud-detection/2.0/fraud_model_v2.onnx" | \
jq '{
  modelName: .modelName,
  version: .version,
  size: .size,
  lastModified: .lastModified,
  etag: .etag
}'
```

---

## 🗑️ **Scenario 5: Cleanup e Manutenzione**

### **Rimozione Versioni Obsolete**
```bash
# Lista versioni per vedere cosa eliminare
curl "http://localhost:8080/api/v1/models/minio/versions/fraud-detection"

# Elimina versioni di backup vecchie
curl -X DELETE "http://localhost:8080/api/v1/models/minio/fraud-detection/1.0-backup/fraud_model_v1.onnx"

# Elimina versioni di sviluppo scadute
curl -X DELETE "http://localhost:8080/api/v1/models/minio/recommendation-engine/dev-1.0/recommendation_dev.onnx?bucketName=development-models"
```

---

## 🎭 **Scenario 6: Test A/B con Modelli**

### **Deploy Variante A**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=recommendation-engine" \
  -F "version=v2.0-variant-a" \
  -F "file=@recommendation_v2_a.onnx"
```

### **Deploy Variante B**
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=recommendation-engine" \
  -F "version=v2.0-variant-b" \
  -F "file=@recommendation_v2_b.onnx"
```

### **Verifica Entrambe le Varianti**
```bash
# Info variante A
curl "http://localhost:8080/api/v1/models/minio/info/recommendation-engine/v2.0-variant-a/recommendation_v2_a.onnx"

# Info variante B
curl "http://localhost:8080/api/v1/models/minio/info/recommendation-engine/v2.0-variant-b/recommendation_v2_b.onnx"

# Confronta dimensioni
curl "http://localhost:8080/api/v1/models/minio/size/recommendation-engine/v2.0-variant-a"
curl "http://localhost:8080/api/v1/models/minio/size/recommendation-engine/v2.0-variant-b"
```

---

## 🚨 **Scenario 7: Disaster Recovery**

### **Backup di Emergenza**
```bash
# Lista tutti i modelli critici
curl "http://localhost:8080/api/v1/models/minio/versions/fraud-detection"
curl "http://localhost:8080/api/v1/models/minio/versions/recommendation-engine"

# Crea backup di sicurezza
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=fraud-detection" \
  -d "sourceVersion=2.0" \
  -d "targetVersion=2.0-emergency-backup" \
  -d "fileName=fraud_model_v2.onnx"
```

### **Ripristino da Backup**
```bash
# In caso di corruzione, ripristina da backup
curl -X POST "http://localhost:8080/api/v1/models/minio/copy" \
  -d "modelName=fraud-detection" \
  -d "sourceVersion=2.0-emergency-backup" \
  -d "targetVersion=2.0-restored" \
  -d "fileName=fraud_model_v2.onnx"
```

---

## 🔍 **Scenario 8: Debug e Troubleshooting**

### **Verifica Connessione**
```bash
# Test health generale
curl "http://localhost:8080/api/v1/minio/health"

# Lista bucket disponibili
curl "http://localhost:8080/api/v1/minio/buckets"
```

### **Debug Upload Fallito**
```bash
# Test upload semplice
echo "test content" > debug-test.txt
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=debug-test" \
  -F "version=1.0" \
  -F "file=@debug-test.txt" \
  -v

# Verifica se è stato caricato
curl "http://localhost:8080/api/v1/models/minio/info/debug-test/1.0/debug-test.txt"

# Cleanup
rm debug-test.txt
curl -X DELETE "http://localhost:8080/api/v1/models/minio/debug-test/1.0/debug-test.txt"
```

---

## 📋 **Script Bash per Automazione Completa**

```bash
#!/bin/bash
# Script per test scenario completo

set -e

BASE_URL="http://localhost:8080"
MINIO_API="$BASE_URL/api/v1/models/minio"

echo "🚀 Inizio test scenario completo..."

# Scenario: Deploy completo di un modello
MODEL_NAME="customer-churn"
VERSION_1="1.0"
VERSION_2="1.1"

echo "📤 Step 1: Upload versione $VERSION_1"
echo "Test model content v1.0" > test_model_v1.txt
curl -X POST "$MINIO_API/upload" \
  -F "modelName=$MODEL_NAME" \
  -F "version=$VERSION_1" \
  -F "file=@test_model_v1.txt"

echo -e "\n✅ Step 2: Verifica upload"
curl "$MINIO_API/info/$MODEL_NAME/$VERSION_1/test_model_v1.txt"

echo -e "\n📤 Step 3: Upload versione $VERSION_2"
echo "Test model content v1.1 - improved" > test_model_v1.1.txt
curl -X POST "$MINIO_API/upload" \
  -F "modelName=$MODEL_NAME" \
  -F "version=$VERSION_2" \
  -F "file=@test_model_v1.1.txt"

echo -e "\n📋 Step 4: Lista versioni"
curl "$MINIO_API/versions/$MODEL_NAME"

echo -e "\n📊 Step 5: Confronta dimensioni"
echo "Versione $VERSION_1:"
curl "$MINIO_API/size/$MODEL_NAME/$VERSION_1"
echo -e "\nVersione $VERSION_2:"
curl "$MINIO_API/size/$MODEL_NAME/$VERSION_2"

echo -e "\n🗑️ Step 6: Cleanup"
curl -X DELETE "$MINIO_API/$MODEL_NAME/$VERSION_1/test_model_v1.txt"
curl -X DELETE "$MINIO_API/$MODEL_NAME/$VERSION_2/test_model_v1.1.txt"

# Cleanup file locali
rm -f test_model_v1.txt test_model_v1.1.txt

echo -e "\n✅ Test scenario completato con successo!"
```

---

## 💡 **Tips per Test Efficaci**

### **1. Preparazione File di Test**
```bash
# Crea file ONNX simulato
dd if=/dev/zero of=fake_model.onnx bs=1024 count=1024  # 1MB file
```

### **2. Test con File Reali**
Se hai file ONNX reali, usali per test più significativi:
```bash
curl -X POST "http://localhost:8080/api/v1/models/minio/upload" \
  -F "modelName=real-iris-classifier" \
  -F "version=1.0" \
  -F "file=@iris.onnx"
```

### **3. Monitoring Continuo**
```bash
# Script per monitoraggio dimensioni
while true; do
  echo "$(date): Checking storage usage..."
  curl -s "http://localhost:8080/api/v1/minio/buckets" | jq .
  sleep 60
done
```

Questi scenari coprono tutti i casi d'uso reali del tuo sistema ML! 🎯
