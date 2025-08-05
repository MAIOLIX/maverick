"# Maverick ML Model Management System

## 🎯 **Overview**
Sistema enterprise per la gestione completa di modelli ML con integrazione PostgreSQL, MinIO e cache in memoria.

## 🚀 **Funzionalità Principali**
- **Upload/Load/Predict/Remove/Delete** - Lifecycle completo modelli ML
- **Database PostgreSQL** - Metadati, versioning, audit
- **Storage MinIO** - File storage distribuito
- **Memory Cache** - Cache in memoria per performance
- **Auto-loading** - Caricamento automatico modelli attivi all'avvio
- **REST API** - 9 endpoint per gestione completa

## 📁 **Architettura**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │  Memory Cache   │    │   PostgreSQL    │
│ (9 endpoints)   │◄──►│ (ModelRegistry) │◄──►│   (Metadata)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
          │                       │                       │
          ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MinIO         │    │  Model Handlers │    │  Bootstrap      │
│ (File Storage)  │◄──►│  (ONNX/PMML)   │◄──►│  (Auto-load)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🛠 **Setup Rapido**

### Prerequisites:
```bash
# PostgreSQL
docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=maverick -e POSTGRES_PASSWORD=maverick postgres:15

# MinIO
docker run -d --name minio -p 9000:9000 -e MINIO_ROOT_USER=minioadmin -e MINIO_ROOT_PASSWORD=minioadmin minio/minio server /data
```

### Avvio:
```bash
mvn spring-boot:run
```

## 📡 **API Endpoints**

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/v1/maverick/upload` | Upload modello (inattivo) |
| POST | `/api/v1/maverick/load` | Attiva modello in memoria |
| POST | `/api/v1/maverick/predict` | Inferenza modello |
| DELETE | `/api/v1/maverick/remove` | Disattiva modello |
| DELETE | `/api/v1/maverick/delete` | Elimina modello completamente |
| GET | `/api/v1/maverick/models-in-memory` | Lista modelli in cache |
| GET | `/api/v1/maverick/models-database` | Lista modelli in database |
| POST | `/api/v1/maverick/bootstrap/reload` | Ricarica modelli attivi |
| GET | `/api/v1/maverick/bootstrap/audit` | Audit consistenza sistema |

## 🧪 **Testing**
```bash
# Test completi
mvn test

# Test specifici
mvn test -Dtest=MaverickControllerIntegrationTest
mvn test -Dtest=ModelDatabaseServiceIntegrationTest
mvn test -Dtest=MinioModelRepositoryIntegrationTest
```

## 📊 **Stato Progetto**
- ✅ **Controller completo** (9 endpoints)
- ✅ **Integrazione DB + MinIO + Cache**
- ✅ **Bootstrap auto-loading**
- ✅ **Test di integrazione completi**
- ✅ **Gestione errori robusta**

## 📝 **Configurazione**
Vedi `application.properties` per configurazione database e MinIO." 
