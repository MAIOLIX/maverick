# Sistema di Autenticazione JWT - Maverick

Il sistema di autenticazione di Maverick supporta due tipi di accesso:

## 🔐 Tipi di Autenticazione

### 1. **Utenti Umani** (Username/Password)
- **Endpoint**: `POST /api/auth/login`
- **Uso**: Login tramite interfaccia web o applicazioni client
- **Token TTL**: 15 minuti (configurabile)
- **Ruoli**: ADMIN, PREDICTOR

### 2. **Client API** (Client Credentials)
- **Endpoint**: `POST /api/auth/token`
- **Uso**: Autenticazione machine-to-machine
- **Token TTL**: 24 ore (configurabile)
- **Accessi**: Admin o Solo Predizioni

## 🚀 Quick Start

### 1. Setup Database
```sql
-- Eseguire il file SQL per creare le tabelle e dati di test
psql -d maverickDB -f database/auth_test_data.sql
```

### 2. Avviare l'Applicazione
```bash
mvn spring-boot:run
```

### 3. Test Login Utente
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Risposta**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "username": "admin",
  "email": "admin@maverick.com",
  "role": "ADMIN"
}
```

### 4. Test Client API
```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"clientId":"maverick-admin-client","clientSecret":"admin-secret-2024"}'
```

## 🔑 Credenziali di Test

### Utenti Umani
| Username | Password | Ruolo | Accesso |
|----------|----------|-------|---------|
| admin | admin123 | ADMIN | Completo (upload, predict, manage) |
| predictor | predictor123 | PREDICTOR | Solo predizioni |

### Client API
| Client ID | Client Secret | Tipo | Scopes |
|-----------|---------------|------|--------|
| maverick-admin-client | admin-secret-2024 | Admin | upload,predict,schema,manage |
| maverick-predictor-client | predictor-secret-2024 | Predictor | predict,schema |

## 🛡️ Autorizzazioni Endpoint

### Endpoint Pubblici
- `POST /api/auth/login` - Login utenti
- `POST /api/auth/token` - Autenticazione client
- `POST /api/auth/validate` - Validazione token
- `GET /api/auth/me` - Info utente corrente
- `/swagger-ui/**` - Documentazione API

### Endpoint Protetti

#### Solo ADMIN
- `POST /api/models/upload/**` - Upload modelli
- `DELETE /api/models/delete/**` - Cancellazione modelli
- `POST /api/models/manage/**` - Gestione modelli

#### ADMIN + PREDICTOR
- `POST /api/models/predict/**` - Predizioni
- `GET /api/models/*/input-schema` - Schema input
- `GET /api/models/*/output-schema` - Schema output
- `GET /api/models/list` - Lista modelli

## 🔧 Configurazione

### application.properties
```properties
# Sicurezza JWT
maverick.security.enabled=true
maverick.jwt.secret=your-super-secret-key
maverick.jwt.user-expiration=900000      # 15 min
maverick.jwt.client-expiration=86400000  # 24 ore

# Cache per performance
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=5m
```

## 📝 Uso del Token

### Header Authorization
```bash
Authorization: Bearer <your-jwt-token>
```

### Esempio Richiesta
```bash
curl -X POST http://localhost:8080/api/models/predict/my-model \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"data": [1, 2, 3, 4, 5]}'
```

## 🔍 Informazioni Token

### Struttura JWT
```json
{
  "sub": "admin",                    // Username o Client ID
  "user_id": 1,                     // ID utente (solo utenti umani)
  "client_id": 1,                   // ID client (solo client API)
  "user_type": "HUMAN",             // HUMAN o MACHINE
  "email": "admin@maverick.com",    // Solo utenti umani
  "scopes": "upload,predict",       // Solo client API
  "iat": 1640995200,               // Issued at
  "exp": 1640995800                // Expiration
}
```

### Validazione Token
Il sistema verifica:
- ✅ Firma JWT valida
- ✅ Token non scaduto
- ✅ Utente/Client attivo nel database
- ✅ Ruolo corrente (query DB in tempo reale)

## 🚨 Sicurezza

### Features di Sicurezza
- **Stateless**: Token JWT autocontenuto
- **Role-based**: Controllo accessi granulare
- **Database verification**: Verifica ruoli in tempo reale
- **Cache ottimizzato**: Performance con sicurezza
- **Scadenza token**: TTL configurabile
- **BCrypt hashing**: Password sicure (costo 12)

### Best Practices
1. **Cambiare secret** in produzione
2. **HTTPS only** in produzione
3. **Token rotation** per client critici
4. **Monitoraggio accessi** via logs
5. **Revoca immediata** disattivando user/client nel DB

## 🔮 Migrazione Futura

Il sistema è progettato per supportare **Keycloak**:
- Campo `keycloak_subject` già presente
- Architettura compatibile con OIDC
- Ruoli mantenuti nel database locale
- Migrazione incrementale possibile

## 📊 Monitoring e Debug

### Logs
```bash
# Abilita debug per JWT
logging.level.com.maiolix.maverick.security=DEBUG
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Swagger UI
Accedi alla documentazione: http://localhost:8080/swagger-ui.html
