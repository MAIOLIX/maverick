# 🗄️ Storage Abstraction - Maverick Model Repository

## 📋 Panoramica

Maverick implementa un'architettura astratta per lo storage dei modelli ML che supporta diversi provider di storage cloud attraverso il pattern Strategy/Repository.

## 🏗️ Architettura

```
IModelStorageRepository (Interface)
├── MinioModelRepository (Implementazione MinIO) ✅
├── AzureBlobModelRepository (Implementazione Azure) 🚧
├── AwsS3ModelRepository (Futuro) 📋
└── GoogleCloudModelRepository (Futuro) 📋
```

## 🔧 Componenti Principali

### 1. **IModelStorageRepository**
Interfaccia astratta che definisce le operazioni standard:
- `uploadModel()` - Caricamento modelli
- `downloadModel()` - Download modelli  
- `deleteModel()` - Eliminazione modelli
- `testConnection()` - Test connettività
- `getProviderType()` - Tipo di provider

### 2. **StorageOperationException**
Eccezione dedicata per errori di storage con supporto multi-provider:
- Messaggio con prefix del provider
- Gestione cause specifiche
- Tracciabilità errori per provider

### 3. **StorageProviderType**
Enum per identificare i provider supportati:
- `MINIO` - MinIO Object Storage
- `AZURE_BLOB` - Azure Blob Storage
- `AWS_S3` - Amazon S3
- `GOOGLE_CLOUD` - Google Cloud Storage
- `LOCAL_FILE` - File System locale

## ⚙️ Configurazione

### Selezione Provider
```properties
# Configurazione principale
maverick.storage.provider=minio
```

### Provider MinIO (Predefinito)
```properties
maverick.storage.minio.endpoint=http://localhost:9000
maverick.storage.minio.access-key=minioadmin
maverick.storage.minio.secret-key=minioadmin
maverick.storage.minio.default-bucket=maverick-models
```

### Provider Azure Blob
```properties
maverick.storage.provider=azure
maverick.storage.azure.connection-string=DefaultEndpointsProtocol=https;...
maverick.storage.azure.container-name=maverick-models
```

## 🚀 Utilizzo

### Injection dell'Interfaccia
```java
@Service
public class MyService {
    
    private final IModelStorageRepository storageRepository;
    
    public void uploadModel(String name, String version, InputStream data) {
        storageRepository.uploadModel(name, version, "model.bin", data, size, "application/octet-stream");
    }
}
```

### Switch Automatico Provider
Spring Boot seleziona automaticamente l'implementazione in base alla configurazione:
- `@ConditionalOnProperty` per abilitazione condizionale
- Dependency injection trasparente
- Zero modifiche al codice business

## 🔄 Integrazione Esistente

L'astrazione è **backward compatible**:
- `MaverickController` usa `IModelStorageRepository`
- `ModelBootstrapService` usa `IModelStorageRepository`
- Log dinamici con nome provider
- Zero breaking changes

## 📊 Vantaggi

### ✅ **Flessibilità**
- Cambio provider senza modifiche codice
- Supporto multi-cloud
- Estensibilità futura

### ✅ **Manutenibilità**
- Codice disaccoppiato
- Test isolati per provider
- Gestione errori unificata

### ✅ **Scalabilità**
- Aggiunta nuovi provider facile
- Configurazione centralizzata
- Provider-specific optimization

## 🛠️ Implementazione Nuovo Provider

### 1. Implementa Interface
```java
@Repository
@ConditionalOnProperty(name = "maverick.storage.provider", havingValue = "nuovo-provider")
public class NuovoProviderRepository implements IModelStorageRepository {
    
    @Override
    public StorageProviderType getProviderType() {
        return StorageProviderType.NUOVO_PROVIDER;
    }
    
    // Implementa tutti i metodi...
}
```

### 2. Aggiungi Configurazione
```properties
maverick.storage.provider=nuovo-provider
maverick.storage.nuovo-provider.property1=value1
```

### 3. Aggiorna Enum
```java
public enum StorageProviderType {
    NUOVO_PROVIDER("Nuovo Provider Display Name");
}
```

## 📈 Roadmap

- ✅ **MinIO** - Implementazione completa
- 🚧 **Azure Blob** - Template creato
- 📋 **AWS S3** - Pianificato
- 📋 **Google Cloud** - Pianificato
- 📋 **Local File** - Per sviluppo/test

## 🎯 Best Practices

1. **Usa sempre l'interfaccia** `IModelStorageRepository`
2. **Gestisci StorageOperationException** appropriatamente
3. **Configura timeout** per operazioni I/O
4. **Test multi-provider** durante sviluppo
5. **Monitor specifico per provider** in produzione

## 🔍 Troubleshooting

### Provider Non Trovato
```
No qualifying bean of type 'IModelStorageRepository'
```
**Soluzione**: Verifica `maverick.storage.provider` nelle properties

### Configurazione Mancante
```
StorageOperationException: Impossibile inizializzare client
```
**Soluzione**: Controlla properties specifiche del provider

---

**Nota**: L'astrazione mantiene la compatibilità completa con il codice esistente garantendo zero downtime durante il migration.
