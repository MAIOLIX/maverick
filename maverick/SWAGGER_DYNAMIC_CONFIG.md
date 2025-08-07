# Swagger Dynamic Configuration Guide

## 🎯 Configurazione Dinamica di Swagger

Ora Swagger rileva automaticamente l'indirizzo dove gira l'applicazione e configura i server disponibili.

## 📋 Configurazioni Disponibili

### 1. **Sviluppo Locale**
```bash
# Automatico - rileva localhost:8080
# Nessuna configurazione necessaria
```

### 2. **VPS/Produzione**
```bash
# Nel file .env configurare:
SWAGGER_EXTERNAL_URL=http://your-vps-ip:8080
# oppure con dominio:
SWAGGER_EXTERNAL_URL=https://api.yourdomain.com

SWAGGER_SERVER_DESCRIPTION=Production Server (VPS)
```

### 3. **Docker/Container**
```bash
# Nel docker-compose.vps.yml le variabili sono già configurate:
SWAGGER_EXTERNAL_URL: "${SWAGGER_EXTERNAL_URL:-}"
SWAGGER_SERVER_DESCRIPTION: "${SWAGGER_SERVER_DESCRIPTION:-Production Server}"
```

## 🌐 Server List in Swagger

Swagger mostrerà automaticamente tutti i server disponibili:

1. **Production Server (VPS)** - se configurato `SWAGGER_EXTERNAL_URL`
2. **Custom Server** - se configurato `SWAGGER_SERVER_URL`
3. **Development Server (localhost)** - sempre presente come fallback

## 🔧 Esempi di Configurazione

### Per VPS con IP pubblico:
```bash
SWAGGER_EXTERNAL_URL=http://203.0.113.10:8080
SWAGGER_SERVER_DESCRIPTION=Production VPS Server
```

### Per VPS con dominio:
```bash
SWAGGER_EXTERNAL_URL=https://api.mycompany.com
SWAGGER_SERVER_DESCRIPTION=Production API Server
```

### Per deployment con reverse proxy:
```bash
SWAGGER_EXTERNAL_URL=https://myapp.com/api
SWAGGER_SERVER_DESCRIPTION=Production Server (Nginx Proxy)
```

## 🚀 Accesso a Swagger UI

```bash
# Sviluppo
http://localhost:8080/swagger-ui.html

# Produzione VPS
http://your-vps-ip:8080/swagger-ui.html

# Con dominio
https://api.yourdomain.com/swagger-ui.html
```

## ✅ Vantaggi

- ✅ **Auto-detection**: Rileva automaticamente l'ambiente
- ✅ **Multi-server**: Mostra tutti i server disponibili per test
- ✅ **Produzione-ready**: Configurabile per deployment VPS/cloud
- ✅ **Fallback**: Localhost sempre disponibile per sviluppo
- ✅ **Context-path aware**: Supporta applicazioni con context path

Il dropdown dei server in Swagger UI ora mostrerà automaticamente tutti gli ambienti configurati!
