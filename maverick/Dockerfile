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

# Crea directory necessarie
RUN mkdir -p /app/models /app/logs /app/uploads && \
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
ENTRYPOINT ["sh", "-c", "\
  # Configurazione JVM dinamica basata su memoria disponibile \
  MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo 2147483648); \
  MAX_HEAP=$(expr $MEMORY_LIMIT / 1048576 \\* 75 / 100); \
  INIT_HEAP=$(expr $MAX_HEAP / 2); \
  export JAVA_OPTS=\"-Xmx${MAX_HEAP}m -Xms${INIT_HEAP}m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS\"; \
  echo \"Starting Maverick with JAVA_OPTS: $JAVA_OPTS\"; \
  java $JAVA_OPTS -jar app.jar\""]
