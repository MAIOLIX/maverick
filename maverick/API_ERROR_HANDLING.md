# API Error Handling Documentation

## API Endpoints

### Base Path
Tutti i nuovi endpoint API utilizzano il path base: `/api/v1/models/`

### Backward Compatibility
Per garantire compatibilità con client esistenti, i vecchi endpoint sotto `/models/` sono ancora supportati ma restituiranno errori informativi che indirizzano verso i nuovi endpoint.

### Available Endpoints
- `POST /api/v1/models/upload` - Upload nuovo modello
- `POST /api/v1/models/predict/{modelName}` - Esegue predizione
- `GET /api/v1/models/schema/{modelName}` - Ottiene schema modello
- `GET /api/v1/models/info/{modelName}` - Informazioni modello
- `GET /api/v1/models/list` - Lista modelli disponibili
- `POST /api/v1/models/add` - Aggiunge modello alla cache
- `POST /api/v1/models/remove/{modelName}` - Rimuove modello dalla cache

## Overview
The Maverick ML Model Server provides comprehensive error handling with standardized HTTP status codes and detailed error responses.

## Error Response Format
All error responses follow a consistent structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "path": "/api/v1/models/endpoint",
  "status": 400,
  "timestamp": "2025-08-04 10:30:00",
  "details": "Additional context or help information"
}
```

## HTTP Status Codes

### 200 OK
- **Description**: Request successful
- **Example**: Model uploaded, prediction completed, data retrieved

### 400 Bad Request
- **Error Codes**: 
  - `MODEL_UPLOAD_ERROR`
  - `MODEL_PREDICTION_ERROR` 
  - `INVALID_ARGUMENT`
  - `VALIDATION_ERROR`
  - `ONNX_PREDICTION_ERROR`
  - `ONNX_EXT_PREDICTION_ERROR`
  - `MOJO_PREDICTION_ERROR`
- **Example**:
```json
{
  "error": "MODEL_PREDICTION_ERROR",
  "message": "Input cannot be null",
  "path": "/api/v1/models/predict/1.0/iris-model",
  "status": 400,
  "timestamp": "2025-08-04 10:30:00",
  "details": "Check your input data format and model availability"
}
```

### 404 Not Found
- **Error Code**: `MODEL_NOT_FOUND`
- **Example**:
```json
{
  "error": "MODEL_NOT_FOUND",
  "message": "Model not found: iris-model version: 2.0",
  "path": "/api/v1/models/info/2.0/iris-model",
  "status": 404,
  "timestamp": "2025-08-04 10:30:00"
}
```

### 413 Payload Too Large
- **Error Code**: `FILE_SIZE_EXCEEDED`
- **Example**:
```json
{
  "error": "FILE_SIZE_EXCEEDED",
  "message": "The uploaded file size exceeds the maximum allowed limit",
  "path": "/api/v1/models/upload",
  "status": 413,
  "timestamp": "2025-08-04 10:30:00",
  "details": "Maximum file size allowed is configured in application properties"
}
```

### 422 Unprocessable Entity
- **Error Codes**:
  - `ONNX_MODEL_ERROR`
  - `ONNX_EXT_MODEL_ERROR`
  - `MOJO_MODEL_ERROR`
- **Example**:
```json
{
  "error": "ONNX_MODEL_ERROR",
  "message": "Invalid ONNX model format",
  "path": "/api/v1/models/upload",
  "status": 422,
  "timestamp": "2025-08-04 10:30:00",
  "details": "The uploaded model file format is not valid or corrupted"
}
```

### 500 Internal Server Error
- **Error Code**: `INTERNAL_SERVER_ERROR`
- **Example**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "path": "/api/v1/models/predict/1.0/iris-model",
  "status": 500,
  "timestamp": "2025-08-04 10:30:00",
  "details": "Please contact support if the problem persists"
}
```

## API Endpoints

### POST /api/v1/models/upload
Upload a new model file.

**Success Response:**
```json
"Model uploaded successfully: iris-model version: 1.0"
```

**Error Responses:**
- `400 Bad Request`: Invalid file format, missing parameters
- `413 Payload Too Large`: File size exceeds limit
- `422 Unprocessable Entity`: Invalid model format

### POST /api/v1/models/predict/{version}/{modelName}
Execute prediction on a model.

**Success Response:**
```json
{
  "prediction": [0, 1, 0],
  "probability": [0.1, 0.8, 0.1],
  "predictedClass": "setosa"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid input data
- `404 Not Found`: Model not found

### GET /api/v1/models/schema/{version}/{modelName}
Get input schema for a model.

**Success Response:**
```json
{
  "inputs": {
    "sepal_length": {
      "type": "float",
      "required": true
    },
    "sepal_width": {
      "type": "float", 
      "required": true
    }
  },
  "modelName": "iris-model",
  "modelVersion": "1.0"
}
```

**Error Responses:**
- `404 Not Found`: Model not found

### GET /api/v1/models/info/{version}/{modelName}
Get detailed model information.

**Success Response:**
```json
{
  "modelName": "iris-model",
  "version": "1.0",
  "type": "ONNX",
  "hasLabelMapping": true,
  "labelMapping": {
    "0": "setosa",
    "1": "versicolor", 
    "2": "virginica"
  }
}
```

**Error Responses:**
- `404 Not Found`: Model not found

### GET /api/v1/models/list
Get all models in cache.

**Success Response:**
```json
{
  "totalModels": 2,
  "models": [
    {
      "modelName": "iris-model",
      "type": "ONNX",
      "version": "1.0",
      "key": "iris-model:1.0",
      "hasLabelMapping": true,
      "labelMappingSize": 3
    }
  ]
}
```

### DELETE /api/v1/models/{version}/{modelName}
Remove a model from cache.

**Success Response:**
```json
"Model removed successfully: iris-model version: 1.0"
```
or
```json
"Model not found: iris-model version: 1.0"
```

## Error Handling Best Practices

1. **Check HTTP Status Code**: Always check the HTTP status code first
2. **Parse Error Response**: Use the structured error response for detailed information
3. **Handle Specific Error Codes**: Implement specific handling for different error codes
4. **Use Details Field**: The details field provides additional context for troubleshooting
5. **Log Timestamp**: Use the timestamp for correlation with server logs

## Client Implementation Example

```javascript
try {
  const response = await fetch('/api/v1/models/predict/1.0/iris-model', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(inputData)
  });
  
  if (!response.ok) {
    const errorData = await response.json();
    
    switch (errorData.error) {
      case 'MODEL_NOT_FOUND':
        console.error('Model not available:', errorData.message);
        break;
      case 'MODEL_PREDICTION_ERROR':
        console.error('Invalid input:', errorData.details);
        break;
      default:
        console.error('Unexpected error:', errorData.message);
    }
    
    throw new Error(errorData.message);
  }
  
  const result = await response.json();
  return result;
  
} catch (error) {
  console.error('API call failed:', error.message);
  throw error;
}
```
