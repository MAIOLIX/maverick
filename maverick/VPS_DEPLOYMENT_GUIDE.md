# 🚀 MAVERICK VPS DEPLOYMENT GUIDE

Guida rapida per il deployment del container Maverick su VPS con PostgreSQL e MinIO esterni.

## 📋 Prerequisiti

- ✅ VPS con Docker e Docker Compose installati
- ✅ PostgreSQL funzionante (esterno)
- ✅ MinIO funzionante (esterno)
- ✅ Proxy Manager configurato (Nginx Proxy Manager, Traefik, etc.)

## 🔧 Configurazione Rapida

### 1. Copia il file di configurazione
```bash
cp .env.example .env
```

### 2. Modifica il file .env con i tuoi parametri
```bash
nano .env
```

**Parametri ESSENZIALI da modificare:**
```bash
# Database PostgreSQL esistente
DATABASE_URL=jdbc:postgresql://your-postgres-host:5432/maverick
DATABASE_USER=maverick_user
DATABASE_PASSWORD=your_secure_password

# MinIO esistente
MINIO_ENDPOINT=http://your-minio-host:9000
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key

# JWT Security (IMPORTANTE!)
JWT_SECRET=your-super-secure-jwt-secret-key-at-least-256-bits-long

# CORS per il tuo dominio
CORS_ORIGINS=https://your-domain.com
```

### 3. Deploy automatico
```bash
chmod +x deploy-vps.sh
./deploy-vps.sh deploy
```

## 🎯 Deploy Passo-Passo

### Opzione A: Deploy Automatico (Raccomandato)
```bash
# Verifica prerequisiti
./deploy-vps.sh check

# Deploy completo (build + start + health check)
./deploy-vps.sh deploy

# Mostra logs in tempo reale
./deploy-vps.sh logs
```

### Opzione B: Deploy Manuale
```bash
# Build dell'immagine
docker-compose -f docker-compose.vps.yml build

# Avvio del container
docker-compose -f docker-compose.vps.yml up -d

# Verifica status
docker-compose -f docker-compose.vps.yml ps
```

## 🔍 Verifica Deployment

### Health Check
```bash
# Via script
./deploy-vps.sh health

# Manuale
curl http://localhost:8080/actuator/health
```

### Logs
```bash
# Via script
./deploy-vps.sh logs

# Manuale
docker logs maverick-app -f
```

### Status Container
```bash
./deploy-vps.sh status
```

## 🌐 Configurazione Proxy Manager

Nel tuo proxy manager (es. Nginx Proxy Manager):

1. **Crea nuovo Proxy Host**
   - Domain Names: `your-domain.com`
   - Forward Hostname/IP: `IP_DEL_SERVER`
   - Forward Port: `8080` (o il valore di MAVERICK_PORT)

2. **SSL Certificate**
   - Configura Let's Encrypt per HTTPS

3. **Advanced Configuration (opzionale)**
   ```nginx
   # Timeout per upload grossi
   client_max_body_size 100M;
   proxy_read_timeout 300s;
   proxy_connect_timeout 75s;
   ```

## 🎮 Comandi Utili

```bash
# Verifica prerequisiti e connessioni
./deploy-vps.sh check

# Build dell'immagine
./deploy-vps.sh build

# Deploy completo
./deploy-vps.sh deploy

# Avvia container
./deploy-vps.sh start

# Ferma container
./deploy-vps.sh stop

# Riavvia container
./deploy-vps.sh restart

# Mostra status
./deploy-vps.sh status

# Mostra logs
./deploy-vps.sh logs

# Health check
./deploy-vps.sh health

# Cleanup completo
./deploy-vps.sh cleanup
```

## ⚙️ Configurazioni Avanzate

### Limiti Risorse
Nel file `.env`:
```bash
MEMORY_LIMIT=4G          # Limite memoria
MEMORY_RESERVATION=2G    # Memoria riservata
CPU_LIMIT=2.0           # Limite CPU cores
CPU_RESERVATION=1.0     # CPU riservata
```

### JVM Tuning
```bash
JAVA_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError
```

### Path Personalizzato
```bash
CONTEXT_PATH=/api/maverick  # App disponibile su /api/maverick
```

## 🛠️ Troubleshooting

### Container non si avvia
```bash
# Verifica logs
docker logs maverick-app

# Verifica configurazione
./deploy-vps.sh check
```

### Problemi di connessione database
```bash
# Test manuale connessione PostgreSQL
docker run --rm postgres:15 pg_isready -h YOUR_DB_HOST -p 5432

# Verifica variabili environment
docker exec maverick-app env | grep DATABASE
```

### Problemi MinIO
```bash
# Test connessione MinIO
curl -I http://your-minio-host:9000/health

# Verifica configurazione MinIO
docker exec maverick-app env | grep MINIO
```

### Health Check Fallisce
```bash
# Verifica health endpoint
curl http://localhost:8080/actuator/health

# Verifica memoria container
docker stats maverick-app
```

## 🔄 Aggiornamenti

### Update dell'applicazione
```bash
# Pull nuove modifiche
git pull

# Rebuild e redeploy
./deploy-vps.sh deploy
```

### Backup dei dati
```bash
# Backup volumi Docker
docker run --rm -v maverick-uploads:/source -v $(pwd):/backup alpine tar czf /backup/maverick-uploads-backup.tar.gz -C /source .
```

## 📊 Monitoring

### Metriche disponibili
- Health: `http://your-domain.com/actuator/health`
- Info: `http://your-domain.com/actuator/info`
- Metrics: `http://your-domain.com/actuator/metrics`

### Log Rotation
I log sono automaticamente ruotati con:
- Max 10MB per file
- Max 100MB totali
- Mantenuti per 30 giorni

## 🔒 Security Checklist

- ✅ JWT_SECRET cambiato dal default
- ✅ Database password sicura
- ✅ MinIO credentials sicure
- ✅ CORS configurato correttamente
- ✅ HTTPS configurato nel proxy
- ✅ Firewall configurato (solo porte necessarie)
- ✅ Container runs as non-root user

## 💡 Tips

1. **Performance**: Regola MEMORY_LIMIT in base alle risorse VPS
2. **Security**: Usa un JWT_SECRET robusto (almeno 256 bit)
3. **CORS**: Specifica domini esatti invece di wildcard
4. **Backup**: Schedula backup automatici dei volumi Docker
5. **Monitoring**: Configura alerting sui logs di errore
