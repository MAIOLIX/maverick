# 📝 Esempi Request POST per Models Repository

## 🎯 Endpoint
```
POST /api/v1/models/repository
Content-Type: application/json
```

## 📋 Esempi di Request

### 1. Modello Locale (ONNX)
```json
{
  "modelName": "iris-classifier",
  "version": "1.0.0",
  "type": "ONNX",
  "description": "Classificatore Iris dataset con ONNX Runtime",
  "storageType": "LOCAL",
  "filePath": "/models/iris/iris-classifier-v1.onnx",
  "fileSize": 2048576,
  "fileHash": "sha256:abc123def456...",
  "contentType": "application/octet-stream",
  "inputSchema": "{\"type\": \"array\", \"shape\": [1, 4], \"dtype\": \"float32\"}",
  "outputSchema": "{\"predictions\": \"array\", \"probabilities\": \"array\"}",
  "metadata": "{\"framework\": \"scikit-learn\", \"accuracy\": 0.95}",
  "createdBy": "data-scientist"
}
```

### 2. Modello Amazon S3
```json
{
  "modelName": "fraud-detector",
  "version": "2.1.0",
  "type": "ONNX",
  "description": "Modello per rilevamento frodi finanziarie",
  "storageType": "S3",
  "filePath": "models/fraud/v2.1.0/fraud-detector.onnx",
  "bucketName": "ml-models-prod",
  "bucketRegion": "eu-west-1",
  "storageClass": "STANDARD",
  "fileSize": 15728640,
  "fileHash": "sha256:def789ghi012...",
  "contentType": "application/octet-stream",
  "inputSchema": "{\"features\": {\"amount\": \"float\", \"merchant_id\": \"string\"}}",
  "outputSchema": "{\"fraud_score\": \"float\", \"is_fraud\": \"boolean\"}",
  "metadata": "{\"training_date\": \"2025-08-01\", \"auc_score\": 0.92}",
  "createdBy": "ml-engineer"
}
```

### 3. Modello Azure Blob Storage
```json
{
  "modelName": "sentiment-analyzer",
  "version": "1.5.2",
  "type": "PMML",
  "description": "Analizzatore di sentiment per recensioni",
  "storageType": "AZURE_BLOB",
  "filePath": "nlp/sentiment/v1.5.2/sentiment-model.pmml",
  "bucketName": "nlp-models",
  "bucketRegion": "westeurope",
  "storageClass": "Hot",
  "fileSize": 5242880,
  "fileHash": "sha256:ghi345jkl678...",
  "contentType": "application/xml",
  "inputSchema": "{\"text\": \"string\", \"language\": \"string\"}",
  "outputSchema": "{\"sentiment\": \"string\", \"confidence\": \"float\"}",
  "metadata": "{\"languages\": [\"it\", \"en\"], \"model_type\": \"transformer\"}",
  "createdBy": "nlp-team"
}
```

### 4. Modello Google Cloud Storage
```json
{
  "modelName": "recommendation-engine",
  "version": "3.0.0",
  "type": "MOJO",
  "description": "Sistema di raccomandazione per e-commerce",
  "storageType": "GCS",
  "filePath": "recommendations/v3.0.0/rec-engine.mojo",
  "bucketName": "ml-models-gcp",
  "bucketRegion": "europe-west1",
  "storageClass": "STANDARD",
  "fileSize": 8388608,
  "fileHash": "sha256:jkl901mno234...",
  "contentType": "application/octet-stream",
  "inputSchema": "{\"user_id\": \"string\", \"context\": \"object\"}",
  "outputSchema": "{\"recommendations\": \"array\", \"scores\": \"array\"}",
  "metadata": "{\"algorithm\": \"collaborative_filtering\", \"ndcg_10\": 0.87}",
  "createdBy": "recommendation-team"
}
```

### 5. Modello MinIO
```json
{
  "modelName": "object-detector",
  "version": "1.2.0",
  "type": "ONNX_EXT",
  "description": "Rilevatore di oggetti per video surveillance",
  "storageType": "MINIO",
  "filePath": "computer-vision/detection/v1.2.0/yolo-detector.zip",
  "bucketName": "cv-models",
  "bucketRegion": "us-east-1",
  "storageClass": "STANDARD",
  "fileSize": 52428800,
  "fileHash": "sha256:mno567pqr890...",
  "contentType": "application/zip",
  "inputSchema": "{\"image\": \"base64\", \"confidence_threshold\": \"float\"}",
  "outputSchema": "{\"detections\": \"array\", \"boxes\": \"array\", \"classes\": \"array\"}",
  "metadata": "{\"input_size\": [640, 640], \"classes\": 80, \"anchors\": []}",
  "createdBy": "cv-team"
}
```

## 🛠️ Test con cURL

### Modello Locale
```bash
curl -X POST http://localhost:8080/api/v1/models/repository \
  -H "Content-Type: application/json" \
  -d '{
    "modelName": "test-model",
    "version": "1.0.0",
    "type": "ONNX",
    "description": "Modello di test",
    "storageType": "LOCAL",
    "filePath": "/tmp/test-model.onnx",
    "fileSize": 1024000,
    "fileHash": "sha256:test123",
    "inputSchema": "{\"input\": \"array\"}",
    "outputSchema": "{\"output\": \"array\"}",
    "metadata": "{\"test\": true}",
    "createdBy": "tester"
  }'
```

### Modello S3
```bash
curl -X POST http://localhost:8080/api/v1/models/repository \
  -H "Content-Type: application/json" \
  -d '{
    "modelName": "s3-model",
    "version": "1.0.0",
    "type": "ONNX",
    "description": "Modello su S3",
    "storageType": "S3",
    "filePath": "models/test/model.onnx",
    "bucketName": "test-bucket",
    "bucketRegion": "eu-west-1",
    "storageClass": "STANDARD",
    "fileSize": 2048000,
    "fileHash": "sha256:s3test123",
    "inputSchema": "{\"features\": \"array\"}",
    "outputSchema": "{\"prediction\": \"float\"}",
    "metadata": "{\"cloud\": true}",
    "createdBy": "cloud-tester"
  }'
```

## 📊 Response di Successo
```json
{
  "id": 1,
  "modelName": "iris-classifier",
  "version": "1.0.0",
  "modelUuid": "550e8400-e29b-41d4-a716-446655440000",
  "type": "ONNX",
  "description": "Classificatore Iris dataset con ONNX Runtime",
  "storageType": "LOCAL",
  "filePath": "/models/iris/iris-classifier-v1.onnx",
  "bucketName": null,
  "bucketRegion": null,
  "storageClass": null,
  "fileSize": 2048576,
  "fileHash": "sha256:abc123def456...",
  "contentType": "application/octet-stream",
  "inputSchema": "{\"type\": \"array\", \"shape\": [1, 4], \"dtype\": \"float32\"}",
  "outputSchema": "{\"predictions\": \"array\", \"probabilities\": \"array\"}",
  "metadata": "{\"framework\": \"scikit-learn\", \"accuracy\": 0.95}",
  "predictionCount": 0,
  "lastUsedAt": null,
  "isActive": true,
  "status": "READY",
  "createdAt": "2025-08-05T14:30:00",
  "updatedAt": "2025-08-05T14:30:00",
  "createdBy": "data-scientist",
  "updatedBy": "system"
}
```

## ⚠️ Campi Obbligatori
- `modelName` - Nome del modello
- `version` - Versione del modello  
- `type` - Tipo (ONNX, PMML, MOJO, H2O, ONNX_EXT)
- `filePath` - Percorso del file
- `fileSize` - Dimensione in bytes

## 🔍 Campi Opzionali
- `storageType` - Default: `LOCAL`
- `bucketName` - Solo per storage cloud
- `bucketRegion` - Solo per storage cloud
- `storageClass` - Classe di storage cloud
- `description` - Descrizione del modello
- `fileHash` - Hash SHA256 del file
- `contentType` - Default: `application/octet-stream`
- `inputSchema` - Schema JSON input
- `outputSchema` - Schema JSON output  
- `metadata` - Metadati JSON
- `createdBy` - Default: `system`

## 🚀 URL Generati Automaticamente
Il sistema genera automaticamente l'URL completo basato sul `storageType`:

- **LOCAL**: `file:///models/iris/iris-classifier-v1.onnx`
- **S3**: `s3://ml-models-prod/models/fraud/v2.1.0/fraud-detector.onnx`
- **AZURE_BLOB**: `https://nlpmodels.blob.core.windows.net/nlp/sentiment/v1.5.2/sentiment-model.pmml`
- **GCS**: `gs://ml-models-gcp/recommendations/v3.0.0/rec-engine.mojo`
- **MINIO**: `minio://cv-models/computer-vision/detection/v1.2.0/yolo-detector.zip`
