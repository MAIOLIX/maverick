# Dockerfile per Maverick Platform VPS
# Build ottimizzato per produzione con configurazione esterna

# Stage 1: Build dell'applicazione
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copia file di configurazione Maven
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Dai permessi di esecuzione al Maven Wrapper
RUN chmod +x ./mvnw

# Download delle dipendenze (layer cacheable)
RUN ./mvnw dependency:go-offline -B

# Copia sorgenti e build
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime dell'applicazione
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Installa curl per health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crea utente non-root per sicurezza
RUN groupadd -r maverick && useradd -r -g maverick maverick

# Copia l'artifact dall'immagine builder
COPY --from=builder /app/target/*.jar app.jar

# Copia script di avvio e crea directory necessarie
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh && \
    mkdir -p /app/models /app/logs /app/uploads && \
    chown -R maverick:maverick /app

# Configurazione JVM dinamica basata su memoria container
ENV JAVA_OPTS=""

# Espone la porta dell'applicazione
EXPOSE 8080

# Cambio utente
USER maverick

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Script di avvio con configurazione JVM dinamica
ENTRYPOINT ["./docker-entrypoint.sh"]
