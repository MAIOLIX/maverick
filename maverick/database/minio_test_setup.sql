-- =============================================================================
-- Script di test per MinIO con le porte specifiche della tua macchina
-- =============================================================================

-- Aggiorna la configurazione MinIO se già esiste nel database
UPDATE system_config 
SET config_value = 'localhost:32768', 
    description = 'Endpoint MinIO API (porta 9000 mappata su 32768)'
WHERE config_key = 'minio_endpoint';

-- Inserisci configurazione console MinIO se non esiste
INSERT INTO system_config (config_key, config_value, description) 
VALUES ('minio_console_endpoint', 'localhost:32769', 'Endpoint MinIO Console (porta 9001 mappata su 32769)')
ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description;

-- Aggiungi configurazioni aggiuntive per MinIO
INSERT INTO system_config (config_key, config_value, description) VALUES
    ('minio_access_key', 'minioadmin', 'Access Key MinIO (default)'),
    ('minio_secret_key', 'minioadmin', 'Secret Key MinIO (default)'),
    ('minio_use_ssl', 'false', 'Usa SSL per connessioni MinIO'),
    ('minio_default_bucket', 'maverick-models', 'Bucket predefinito per i modelli')
ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description;

-- Esempio di modello di test su MinIO
INSERT INTO models (
    model_name, version, type, description, storage_type, file_path, file_size, 
    bucket_name, bucket_region, storage_class,
    input_schema, metadata, created_by
) VALUES 
(
    'test-minio-model',
    '1.0',
    'ONNX',
    'Modello di test per MinIO locale - porta 32768',
    'MINIO',
    'models/test/test-model-v1.0.onnx',
    1024000,
    'maverick-models', -- bucket_name per MinIO
    'us-east-1', -- region (non usata da MinIO ma richiesta dallo schema)
    'STANDARD', -- storage_class
    '{"type": "object", "properties": {"input": {"type": "array", "items": {"type": "number"}}}}',
    '{"accuracy": 0.95, "dataset": "test_data", "algorithm": "test", "features": 10, "author": "Test User", "minio_endpoint": "localhost:32768"}',
    'test-user'
)
ON CONFLICT (model_name, version) DO UPDATE SET 
    description = EXCLUDED.description,
    metadata = EXCLUDED.metadata,
    updated_at = CURRENT_TIMESTAMP;

-- Query per verificare la configurazione MinIO
SELECT config_key, config_value, description 
FROM system_config 
WHERE config_key LIKE 'minio%' 
ORDER BY config_key;

-- Query per vedere il modello MinIO di test
SELECT id, model_name, version, storage_type, bucket_name, file_path, metadata->>'minio_endpoint' as endpoint
FROM models 
WHERE storage_type = 'MINIO';
