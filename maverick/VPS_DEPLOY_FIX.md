# Deployment VPS - Complete Fix Guide

## Problemi Risolti

### 1. ❌ Errore File .env (RISOLTO)
**Problema**: `.env: line 68: -XX:HeapDumpPath=/app/logs: No such file or directory`
**Soluzione**: Rimossi parametri JVM mal formattati, creato script di avvio separato

### 2. ❌ Errore Spring Profiles (RISOLTO)
**Problema**: `Property 'spring.profiles.active' imported from location 'application-production.properties' is invalid`
**Soluzione**: Rimosso `spring.profiles.active` da `application-production.properties`

### 4. ❌ Health Check Loop (RISOLTO)
**Problema**: `/actuator/health sta in loop in avvio` - Endpoint Actuator non disponibile
**Soluzione**: 
- Aggiunta dipendenza `spring-boot-starter-actuator` al pom.xml
- Creato `HealthController` con endpoint semplice `/health`
- Migliorato Docker HEALTHCHECK con fallback
- Configurato Actuator per produzione
**Problema**: `Failed to connect to localhost:32768` - MinIO non raggiungibile
**Soluzione**: Corrette configurazioni MinIO in `application-production.properties`

## Configurazioni Corrette

### File .env per VPS
```bash
# Database PostgreSQL esterno
DATABASE_URL=jdbc:postgresql://YOUR-DB-HOST:5432/maverick
DATABASE_USER=maverick
DATABASE_PASSWORD=YOUR-SECURE-PASSWORD

# MinIO esterno - IMPORTANTE: usare IP pubblico del VPS
MINIO_ENDPOINT=http://YOUR-VPS-IP:9000
MINIO_ACCESS_KEY=your-minio-access-key
MINIO_SECRET_KEY=your-minio-secret-key
MINIO_BUCKET=maverick-models

# Security
JWT_SECRET=your-super-secure-jwt-secret-key
```

### Test Senza MinIO
Per testare l'applicazione senza MinIO esterno:
```bash
# Usa profilo test che disabilita MinIO
SPRING_PROFILES_ACTIVE=test-vps ./deploy-vps.sh deploy
```

## Deployment Steps

### Opzione 1: Deployment Completo (con MinIO)
```bash
# 1. Copia template produzione
cp .env.production .env

# 2. Modifica .env con i tuoi parametri reali
nano .env

# 3. Assicurati che MinIO sia raggiungibile
telnet YOUR-VPS-IP 9000

# 4. Deploy
./deploy-vps.sh deploy
```

### Opzione 2: Test Deployment (senza MinIO)
```bash
# 1. Test build
docker build -t maverick:test .

# 2. Test run con profilo test
docker run -d --name maverick-test \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=test-vps \
  maverick:test

# 3. Verifica logs
docker logs maverick-test
```

## Troubleshooting

### MinIO Connection Issues
- Verificare che MinIO sia in esecuzione sull'host esterno
- Controllare firewall e porte aperte (9000, 9001)
- Usare IP pubblico del VPS, non localhost
- Testare connessione: `telnet YOUR-VPS-IP 9000`

### Database Connection Issues  
- Verificare PostgreSQL esterno raggiungibile
- Controllare credenziali DATABASE_URL
- Testare connessione: `telnet YOUR-DB-HOST 5432`

Il deployment ora dovrebbe funzionare correttamente con tutte le correzioni applicate!
