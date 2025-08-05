-- =============================================================================
-- Schema PostgreSQL per Maverick ML Model Management System
-- =============================================================================

-- Estensioni PostgreSQL utili
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- Per ricerche full-text efficienti

-- =============================================================================
-- Tabella principale per i modelli ML
-- =============================================================================
CREATE TABLE models (
    id BIGSERIAL PRIMARY KEY,
    
    -- Identificazione modello
    model_name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    model_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Metadati del modello
    type VARCHAR(50) NOT NULL, -- 'ONNX', 'PMML', 'MOJO', 'H2O'
    description TEXT,
    
    -- Storage location (supporta file system locale e cloud buckets)
    storage_type VARCHAR(20) NOT NULL DEFAULT 'LOCAL', -- 'LOCAL', 'S3', 'AZURE_BLOB', 'GCS'
    file_path VARCHAR(500) NOT NULL, -- Path locale o chiave bucket
    bucket_name VARCHAR(100), -- Nome del bucket (null per storage locale)
    bucket_region VARCHAR(50), -- Regione del bucket
    storage_class VARCHAR(50), -- Classe di storage (STANDARD, INFREQUENT_ACCESS, etc.)
    
    -- Metadati file
    file_size BIGINT NOT NULL,
    file_hash VARCHAR(64), -- SHA-256 hash per integrità
    content_type VARCHAR(100) DEFAULT 'application/octet-stream',
    
    -- Configurazione
    input_schema JSONB, -- Schema degli input in formato JSON
    output_schema JSONB, -- Schema degli output in formato JSON
    metadata JSONB, -- Metadati flessibili (author, accuracy, dataset, etc.)
    
    -- Statistiche di utilizzo
    prediction_count BIGINT DEFAULT 0,
    last_used_at TIMESTAMP WITH TIME ZONE,
    
    -- Stato e controllo
    is_active BOOLEAN DEFAULT true,
    status VARCHAR(20) DEFAULT 'READY', -- 'READY', 'LOADING', 'ERROR', 'MAINTENANCE'
    
    -- Audit trail
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    
    -- Vincoli
    CONSTRAINT uk_models_name_version UNIQUE (model_name, version),
    CONSTRAINT ck_models_type CHECK (type IN ('ONNX', 'PMML', 'MOJO', 'H2O', 'ONNX_EXT')),
    CONSTRAINT ck_models_status CHECK (status IN ('READY', 'LOADING', 'ERROR', 'MAINTENANCE')),
    CONSTRAINT ck_models_storage_type CHECK (storage_type IN ('LOCAL', 'S3', 'AZURE_BLOB', 'GCS', 'MINIO')),
    CONSTRAINT ck_models_file_size CHECK (file_size > 0),
    CONSTRAINT ck_models_bucket_required CHECK (
        (storage_type = 'LOCAL' AND bucket_name IS NULL) OR 
        (storage_type != 'LOCAL' AND bucket_name IS NOT NULL)
    )
);

-- =============================================================================
-- Tabella per lo storico delle predizioni (opzionale, per analytics)
-- =============================================================================
CREATE TABLE prediction_logs (
    id BIGSERIAL PRIMARY KEY,
    model_id BIGINT NOT NULL REFERENCES models(id) ON DELETE CASCADE,
    
    -- Dati della predizione
    input_data JSONB,
    output_data JSONB,
    confidence_score DECIMAL(5,4), -- 0.0000 - 1.0000
    
    -- Metriche di performance
    execution_time_ms INTEGER,
    memory_usage_mb DECIMAL(10,2),
    
    -- Contesto
    client_ip INET,
    user_agent TEXT,
    session_id VARCHAR(100),
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Vincoli
    CONSTRAINT ck_prediction_logs_confidence CHECK (confidence_score >= 0 AND confidence_score <= 1),
    CONSTRAINT ck_prediction_logs_execution_time CHECK (execution_time_ms >= 0)
);

-- =============================================================================
-- Tabella per le configurazioni del sistema
-- =============================================================================
CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    is_encrypted BOOLEAN DEFAULT false,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) DEFAULT 'system'
);

-- =============================================================================
-- Indici per performance
-- =============================================================================

-- Indici principali per la tabella models
CREATE INDEX idx_models_name ON models(model_name);
CREATE INDEX idx_models_type ON models(type);
CREATE INDEX idx_models_active ON models(is_active);
CREATE INDEX idx_models_status ON models(status);
CREATE INDEX idx_models_storage_type ON models(storage_type);
CREATE INDEX idx_models_bucket ON models(bucket_name);
CREATE INDEX idx_models_created_at ON models(created_at);
CREATE INDEX idx_models_last_used ON models(last_used_at);
CREATE INDEX idx_models_prediction_count ON models(prediction_count DESC);

-- Indici per ricerche sui metadati JSON
CREATE INDEX idx_models_metadata_gin ON models USING GIN(metadata);
CREATE INDEX idx_models_input_schema_gin ON models USING GIN(input_schema);

-- Indici per ricerche full-text
CREATE INDEX idx_models_description_gin ON models USING GIN(to_tsvector('english', description));

-- Indici per prediction_logs
CREATE INDEX idx_prediction_logs_model_id ON prediction_logs(model_id);
CREATE INDEX idx_prediction_logs_created_at ON prediction_logs(created_at);
CREATE INDEX idx_prediction_logs_session ON prediction_logs(session_id);

-- Indice composto per query comuni
CREATE INDEX idx_models_active_type_name ON models(is_active, type, model_name);
CREATE INDEX idx_models_storage_bucket ON models(storage_type, bucket_name);

-- =============================================================================
-- Trigger per aggiornamento automatico di updated_at
-- =============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_models_updated_at
    BEFORE UPDATE ON models
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_system_config_updated_at
    BEFORE UPDATE ON system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- Funzioni di utilità
-- =============================================================================

-- Funzione per incrementare il contatore delle predizioni
CREATE OR REPLACE FUNCTION increment_prediction_count(
    p_model_name VARCHAR(100),
    p_version VARCHAR(50)
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE models 
    SET prediction_count = prediction_count + 1,
        last_used_at = CURRENT_TIMESTAMP
    WHERE model_name = p_model_name 
      AND version = p_version 
      AND is_active = true;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Funzione per ottenere statistiche del modello
CREATE OR REPLACE FUNCTION get_model_statistics()
RETURNS TABLE(
    total_models BIGINT,
    total_predictions BIGINT,
    avg_file_size DECIMAL,
    most_used_model VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_models,
        SUM(prediction_count)::BIGINT as total_predictions,
        AVG(file_size)::DECIMAL as avg_file_size,
        (SELECT model_name FROM models WHERE is_active = true ORDER BY prediction_count DESC LIMIT 1) as most_used_model
    FROM models 
    WHERE is_active = true;
END;
$$ LANGUAGE plpgsql;

-- Funzione per ottenere statistiche di storage
CREATE OR REPLACE FUNCTION get_storage_statistics()
RETURNS TABLE(
    storage_type VARCHAR(20),
    model_count BIGINT,
    total_size_mb DECIMAL,
    avg_size_mb DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.storage_type,
        COUNT(*)::BIGINT as model_count,
        ROUND((SUM(m.file_size)::DECIMAL / 1024 / 1024), 2) as total_size_mb,
        ROUND((AVG(m.file_size)::DECIMAL / 1024 / 1024), 2) as avg_size_mb
    FROM models m
    WHERE m.is_active = true
    GROUP BY m.storage_type
    ORDER BY model_count DESC;
END;
$$ LANGUAGE plpgsql;

-- Funzione per ottenere URL completo del modello (da implementare nel codice Java)
CREATE OR REPLACE FUNCTION get_model_storage_info(
    p_model_name VARCHAR(100),
    p_version VARCHAR(50)
)
RETURNS TABLE(
    storage_type VARCHAR(20),
    file_path VARCHAR(500),
    bucket_name VARCHAR(100),
    bucket_region VARCHAR(50),
    full_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.storage_type,
        m.file_path,
        m.bucket_name,
        m.bucket_region,
        CASE 
            WHEN m.storage_type = 'LOCAL' THEN 'file://' || m.file_path
            WHEN m.storage_type = 'S3' THEN 's3://' || m.bucket_name || '/' || m.file_path
            WHEN m.storage_type = 'AZURE_BLOB' THEN 'https://' || split_part(m.bucket_name, '/', 1) || '.blob.core.windows.net/' || m.file_path
            WHEN m.storage_type = 'GCS' THEN 'gs://' || m.bucket_name || '/' || m.file_path
            WHEN m.storage_type = 'MINIO' THEN 'minio://' || m.bucket_name || '/' || m.file_path
            ELSE m.file_path
        END as full_url
    FROM models m
    WHERE m.model_name = p_model_name 
      AND m.version = p_version 
      AND m.is_active = true;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- Dati di esempio per testing
-- =============================================================================
INSERT INTO system_config (config_key, config_value, description) VALUES
    ('max_model_size_mb', '500', 'Dimensione massima per i file dei modelli in MB'),
    ('model_cache_ttl_minutes', '60', 'TTL della cache dei modelli in minuti'),
    ('prediction_log_retention_days', '90', 'Giorni di retention per i log delle predizioni'),
    ('default_model_timeout_seconds', '30', 'Timeout predefinito per le predizioni in secondi'),
    ('s3_default_region', 'eu-west-1', 'Regione AWS S3 predefinita'),
    ('azure_storage_account', 'maverickmodels', 'Nome dell''account Azure Storage'),
    ('gcs_project_id', 'maverick-ml-platform', 'ID progetto Google Cloud'),
    ('minio_endpoint', 'localhost:32768', 'Endpoint MinIO API (porta 9000 mappata su 32768)'),
    ('minio_console_endpoint', 'localhost:32769', 'Endpoint MinIO Console (porta 9001 mappata su 32769)');

-- Modelli di esempio con diversi tipi di storage
INSERT INTO models (
    model_name, version, type, description, storage_type, file_path, file_size, 
    bucket_name, bucket_region, storage_class,
    input_schema, metadata, created_by
) VALUES 
(
    'iris-classifier',
    '1.0',
    'ONNX',
    'Classificatore Iris Dataset - Modello locale di esempio',
    'LOCAL',
    '/models/iris/iris-classifier-v1.0.onnx',
    2048000,
    NULL, -- bucket_name per LOCAL
    NULL, -- bucket_region per LOCAL
    NULL, -- storage_class per LOCAL
    '{"type": "object", "properties": {"sepal_length": {"type": "number"}, "sepal_width": {"type": "number"}, "petal_length": {"type": "number"}, "petal_width": {"type": "number"}}}',
    '{"accuracy": 0.97, "dataset": "iris", "algorithm": "random_forest", "features": 4, "classes": 3, "training_date": "2025-08-05", "author": "ML Team"}',
    'system'
),
(
    'fraud-detector',
    '2.1',
    'ONNX',
    'Rilevatore frodi transazioni - Modello su S3',
    'S3',
    'models/fraud/fraud-detector-v2.1.onnx',
    15728640,
    'maverick-ml-models-prod', -- bucket_name per S3
    'eu-west-1', -- bucket_region per S3
    'STANDARD', -- storage_class per S3
    '{"type": "object", "properties": {"amount": {"type": "number"}, "merchant_category": {"type": "string"}, "hour": {"type": "integer"}, "day_of_week": {"type": "integer"}}}',
    '{"accuracy": 0.94, "dataset": "transactions_2025", "algorithm": "xgboost", "features": 25, "classes": 2, "training_date": "2025-07-20", "author": "Fraud Team"}',
    'ml-engineer'
),
(
    'recommendation-engine',
    '1.5',
    'PMML',
    'Motore di raccomandazioni - Azure Blob Storage',
    'AZURE_BLOB',
    'models/recommendations/rec-engine-v1.5.pmml',
    8388608,
    'maverickmodels/production', -- bucket_name per Azure
    'West Europe', -- bucket_region per Azure
    'Hot', -- storage_class per Azure
    '{"type": "object", "properties": {"user_id": {"type": "string"}, "category_preferences": {"type": "array"}, "purchase_history": {"type": "array"}}}',
    '{"accuracy": 0.89, "dataset": "user_interactions", "algorithm": "collaborative_filtering", "features": 50, "author": "Recommendation Team"}',
    'data-scientist'
);

-- =============================================================================
-- Views utili per query comuni
-- =============================================================================

-- Vista per modelli attivi con statistiche
CREATE VIEW active_models_stats AS
SELECT 
    m.id,
    m.model_name,
    m.version,
    m.type,
    m.description,
    m.storage_type,
    m.bucket_name,
    m.file_size,
    m.prediction_count,
    m.last_used_at,
    m.created_at,
    CASE 
        WHEN m.last_used_at IS NULL THEN 'never_used'
        WHEN m.last_used_at > CURRENT_TIMESTAMP - INTERVAL '1 day' THEN 'active'
        WHEN m.last_used_at > CURRENT_TIMESTAMP - INTERVAL '7 days' THEN 'recent'
        ELSE 'stale'
    END as usage_status
FROM models m
WHERE m.is_active = true
ORDER BY m.prediction_count DESC;

-- Vista per modelli più utilizzati
CREATE VIEW top_models AS
SELECT 
    model_name,
    version,
    type,
    storage_type,
    bucket_name,
    prediction_count,
    last_used_at,
    ROUND((file_size::DECIMAL / 1024 / 1024), 2) as size_mb
FROM models 
WHERE is_active = true 
  AND prediction_count > 0
ORDER BY prediction_count DESC
LIMIT 10;

-- Vista per statistiche di storage per bucket
CREATE VIEW storage_usage_by_bucket AS
SELECT 
    storage_type,
    bucket_name,
    bucket_region,
    COUNT(*) as model_count,
    ROUND((SUM(file_size)::DECIMAL / 1024 / 1024), 2) as total_size_mb,
    ROUND((AVG(file_size)::DECIMAL / 1024 / 1024), 2) as avg_size_mb,
    MIN(created_at) as first_model_date,
    MAX(created_at) as latest_model_date
FROM models 
WHERE is_active = true
GROUP BY storage_type, bucket_name, bucket_region
ORDER BY total_size_mb DESC;

-- =============================================================================
-- Grants (da adattare secondo le tue esigenze di sicurezza)
-- =============================================================================

-- Crea utente applicazione
-- CREATE USER maverick_app WITH PASSWORD 'your_secure_password';

-- Grant permessi base
-- GRANT CONNECT ON DATABASE maverickdb TO maverick_app;
-- GRANT USAGE ON SCHEMA public TO maverick_app;
-- GRANT SELECT, INSERT, UPDATE ON models TO maverick_app;
-- GRANT SELECT, INSERT ON prediction_logs TO maverick_app;
-- GRANT SELECT, UPDATE ON system_config TO maverick_app;
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO maverick_app;

-- =============================================================================
-- Commenti finali
-- =============================================================================

COMMENT ON TABLE models IS 'Tabella principale per la gestione dei modelli ML con supporto multi-storage';
COMMENT ON TABLE prediction_logs IS 'Log delle predizioni per analytics e debugging';
COMMENT ON TABLE system_config IS 'Configurazioni di sistema';

COMMENT ON COLUMN models.storage_type IS 'Tipo di storage: LOCAL, S3, AZURE_BLOB, GCS, MINIO';
COMMENT ON COLUMN models.bucket_name IS 'Nome del bucket cloud (null per storage locale)';
COMMENT ON COLUMN models.bucket_region IS 'Regione del bucket cloud';
COMMENT ON COLUMN models.storage_class IS 'Classe di storage (STANDARD, INFREQUENT_ACCESS, etc.)';
COMMENT ON COLUMN models.input_schema IS 'Schema JSON per validazione degli input';
COMMENT ON COLUMN models.metadata IS 'Metadati flessibili del modello (accuracy, author, etc.)';
COMMENT ON COLUMN models.prediction_count IS 'Contatore delle predizioni eseguite';
COMMENT ON COLUMN models.file_hash IS 'Hash SHA-256 per verificare integrità del file';
COMMENT ON COLUMN models.content_type IS 'MIME type del file del modello';
