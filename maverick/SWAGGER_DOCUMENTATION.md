# Swagger Documentation

## 📖 **Accesso Swagger UI**

```
http://localhost:8080/swagger-ui/index.html
```

## 🎯 **API Documentate**

La documentazione Swagger include tutti i **9 endpoints** del `MaverickController`:

### � **Endpoint Principali:**
- **Upload/Load/Predict/Remove/Delete** - Gestione modelli ML
- **Models In Memory/Database** - Liste e statistiche  
- **Bootstrap Reload/Audit** - Amministrazione sistema

### � **Modelli Dati:**
- **ModelEntity** - Entità database completa
- **Request/Response DTOs** - Tutti i payload API
- **Error responses** - Gestione errori standardizzata

### 🔧 **Configurazione:**
La configurazione Swagger è in `SwaggerConfig.java` con:
- Autenticazione API Key (se abilitata)
- Documentazione endpoint completa
- Esempi request/response
- Schema validazione

## 🧪 **Test API**
Usa Swagger UI per testare tutti gli endpoint direttamente dal browser.
