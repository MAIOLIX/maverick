# Integrazione PostgreSQL con Spring Boot - Repository Layer

## 🎯 Panoramica

L'integrazione con PostgreSQL è stata completata con successo. Il sistema ora include:

- **Entità JPA** per la gestione dei modelli ML con supporto multi-cloud storage
- **Repository Spring Data JPA** con query avanzate
- **Servizio di gestione** con operazioni CRUD e statistiche
- **Controller REST** per l'accesso alle funzionalità
- **Endpoint di test** per verificare l'integrazione

## 📊 Database Schema

### Tabella `models`
La tabella principale per i modelli ML include:

- **Identificazione**: `id`, `model_name`, `version`, `model_uuid`
- **Metadati**: `type`, `description`, `input_schema`, `output_schema`
- **Storage Multi-Cloud**: `storage_type`, `file_path`, `bucket_name`, `bucket_region`, `storage_class`
- **File Info**: `file_size`, `file_hash`, `content_type`
- **Statistiche**: `prediction_count`, `last_used_at`
- **Stato**: `is_active`, `status`
- **Audit**: `created_at`, `updated_at`, `created_by`, `updated_by`

### Storage Types Supportati
- `LOCAL`: File system locale
- `S3`: Amazon S3
- `AZURE_BLOB`: Azure Blob Storage
- `GCS`: Google Cloud Storage
- `MINIO`: MinIO

## 🏗️ Architettura

### 1. Entità JPA (`ModelEntity`)
```java
@Entity
@Table(name = "models")
public class ModelEntity {
    // Mapping completo della tabella PostgreSQL
    // Enum per tipi di modello, storage e stato
    // Metodi di utilità per URL e statistiche
}
```

### 2. Repository (`ModelRepository`)
```java
@Repository
public interface ModelRepository extends JpaRepository<ModelEntity, Long> {
    // Query personalizzate per ricerca
    // Operazioni di aggiornamento con @Modifying
    // Statistiche e aggregazioni
    // Supporto paginazione
}
```

### 3. Servizio (`ModelDatabaseService`)
```java
@Service
@Transactional
public class ModelDatabaseService {
    // Operazioni CRUD
    // Gestione versioni modelli
    // Statistiche e manutenzione
    // Metodi di utilità per test
}
```

### 4. Controller REST (`ModelRepositoryController`)
```java
@RestController
@RequestMapping("/api/v1/models/repository")
public class ModelRepositoryController {
    // Endpoint completi per gestione modelli
    // Ricerca e paginazione
    // Statistiche e manutenzione
}
```

## 🔧 Configurazione

### Database Connection
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/maverickDB
spring.datasource.username=maverick
spring.datasource.password=Alessandro12
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

## 🚀 API Endpoints

### Repository Management
- `GET /api/v1/models/repository` - Lista tutti i modelli
- `GET /api/v1/models/repository/{id}` - Trova per ID
- `GET /api/v1/models/repository/name/{name}/version/{version}` - Trova per nome/versione
- `POST /api/v1/models/repository` - Salva nuovo modello
- `POST /api/v1/models/repository/deploy` - Deploy nuova versione

### Ricerca
- `GET /api/v1/models/repository/search?name={name}` - Ricerca per nome
- `GET /api/v1/models/repository/search/fulltext?searchTerm={term}` - Ricerca full-text
- `GET /api/v1/models/repository/type/{type}` - Trova per tipo
- `GET /api/v1/models/repository/storage/{storageType}` - Trova per storage
- `GET /api/v1/models/repository/cloud` - Lista modelli cloud
- `GET /api/v1/models/repository/bucket/{bucketName}` - Trova per bucket

### Operazioni
- `POST /api/v1/models/repository/{id}/predict` - Registra predizione
- `PUT /api/v1/models/repository/{id}/status` - Aggiorna stato
- `DELETE /api/v1/models/repository/{id}` - Disattiva modello

### Statistiche
- `GET /api/v1/models/repository/stats/count-by-type` - Conta per tipo
- `GET /api/v1/models/repository/stats/count-by-storage` - Conta per storage
- `GET /api/v1/models/repository/stats/total-predictions` - Totale predizioni
- `GET /api/v1/models/repository/stats/most-used` - Più utilizzati

### Test Database
- `GET /api/v1/test/database/status` - Verifica connessione
- `POST /api/v1/test/database/create-sample` - Crea modello test
- `POST /api/v1/test/database/create-cloud-sample` - Crea modello cloud test
- `GET /api/v1/test/database/stats` - Statistiche database

## 🧪 Testing

### Test di Integrazione
```bash
# Test completo repository
mvn test -Dtest=ModelRepositoryIntegrationTest

# Test connessione database
mvn test -Dtest=DatabaseConnectionTest

# Tutti i test
mvn clean test
```

### Test via API
```bash
# Verifica stato database
curl -X GET http://localhost:8080/api/v1/test/database/status

# Crea modello di test
curl -X POST http://localhost:8080/api/v1/test/database/create-sample

# Statistiche
curl -X GET http://localhost:8080/api/v1/test/database/stats
```

## 📈 Funzionalità Avanzate

### Multi-Cloud Storage Support
Il sistema supporta automaticamente diversi tipi di storage:
```java
// Esempio modello S3
ModelEntity s3Model = ModelEntity.builder()
    .modelName("fraud-detector")
    .version("2.0.0")
    .storageType(StorageType.S3)
    .bucketName("ml-models-prod")
    .bucketRegion("eu-west-1")
    .storageClass("STANDARD")
    .filePath("models/fraud/v2.0.0/model.onnx")
    .build();

// URL generato automaticamente: s3://ml-models-prod/models/fraud/v2.0.0/model.onnx
```

### Query Avanzate
```java
// Ricerca semantica
Page<ModelEntity> results = repository.fullTextSearch("fraud detection", pageable);

// Modelli utilizzati di recente
List<ModelEntity> recent = repository.findRecentlyUsed(LocalDateTime.now().minusDays(7));

// Statistiche per tipo di storage
List<Object[]> stats = repository.getStorageSizeByType();
```

### Gestione Versioni
```java
// Deploy automatico con disattivazione versioni precedenti
ModelEntity newVersion = modelDatabaseService.deployNewVersion(modelV2);
// Automaticamente disattiva v1.0.0, attiva v2.0.0
```

### Audit Trail
Ogni modello include:
- Timestamp di creazione e aggiornamento automatici
- Tracciamento dell'utente che ha creato/modificato
- Cronologia delle predizioni e utilizzi

## 🔒 Sicurezza

### Considerazioni
- ⚠️ **Password hardcoded**: Spostare in variabili d'ambiente per produzione
- 🔐 **Connection Pool**: Configurato con HikariCP per performance
- 📝 **Validazione**: Constraint database + validazione JPA
- 🛡️ **Transazioni**: Gestione automatica con `@Transactional`

### Raccomandazioni per Produzione
```properties
# Usa variabili d'ambiente
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}

# SSL per connessioni sicure
spring.datasource.url=jdbc:postgresql://localhost:5432/maverickDB?sslmode=require
```

## 🎉 Risultato

✅ **Database PostgreSQL** integrato con successo  
✅ **Schema completo** con supporto multi-cloud storage  
✅ **Repository Spring Data JPA** con query avanzate  
✅ **API REST** complete per gestione modelli  
✅ **Test di integrazione** funzionanti  
✅ **Endpoint di test** per verifica rapida  

Il sistema è ora pronto per gestire modelli ML con storage distribuito e funzionalità enterprise-grade!
