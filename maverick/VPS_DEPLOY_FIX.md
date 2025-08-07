# Deployment VPS - Quick Fix Guide

## Problema Risolto
L'errore `.env: line 68: -XX:HeapDumpPath=/app/logs: No such file or directory` è stato risolto.

## Soluzioni Implementate

### 1. File .env Corretto
Creato file `.env` con configurazioni corrette per VPS che non causano errori di sintassi bash.

### 2. File .env.production
Creato template pulito per produzione in `.env.production` senza configurazioni JVM problematiche.

### 3. Script Deploy Migliorato
Aggiornato `deploy-vps.sh` con controllo errori migliorato per il caricamento del file `.env`.

## Come Procedere

### Per Deploy Immediato
```bash
# 1. Copia il file di produzione
cp .env.production .env

# 2. Modifica con i tuoi parametri
nano .env

# 3. Esegui il deploy
./deploy-vps.sh deploy
```

### Configurazioni da Modificare in .env
```bash
# Database PostgreSQL
DATABASE_URL=jdbc:postgresql://YOUR-DB-HOST:5432/maverick
DATABASE_USER=maverick
DATABASE_PASSWORD=YOUR-SECURE-PASSWORD

# MinIO Storage
MINIO_ENDPOINT=http://YOUR-MINIO-HOST:9000
MINIO_ACCESS_KEY=YOUR-MINIO-ACCESS-KEY
MINIO_SECRET_KEY=YOUR-MINIO-SECRET-KEY

# Security
JWT_SECRET=YOUR-SUPER-SECURE-JWT-SECRET-KEY

# CORS (per frontend)
CORS_ORIGINS=https://your-domain.com
```

### Verifica Deploy
```bash
# Check status
./deploy-vps.sh check

# Logs
./deploy-vps.sh logs

# Health check
curl http://your-vps-ip:8080/actuator/health
```

## Risoluzione del Problema JVM
- Rimossi parametri JVM mal formattati
- Semplificata configurazione per VPS
- Aggiunto controllo errori nel caricamento .env

Il deploy ora dovrebbe funzionare senza errori di sintassi bash.
