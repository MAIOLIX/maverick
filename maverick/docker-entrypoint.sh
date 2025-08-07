#!/bin/sh

# Script di avvio per Maverick con configurazione JVM dinamica

set -e

echo "Starting Maverick Platform..."

# Configurazione JVM dinamica basata su memoria disponibile
if [ -f /sys/fs/cgroup/memory/memory.limit_in_bytes ]; then
    MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)
else
    MEMORY_LIMIT=2147483648  # Default 2GB
fi

# Calcola heap max (75% della memoria disponibile)
MAX_HEAP=$(expr $MEMORY_LIMIT / 1048576 \* 75 / 100)
INIT_HEAP=$(expr $MAX_HEAP / 2)

# Configura JAVA_OPTS se non già impostato
if [ -z "$JAVA_OPTS" ]; then
    export JAVA_OPTS="-Xmx${MAX_HEAP}m -Xms${INIT_HEAP}m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"
fi

echo "Memory limit: ${MEMORY_LIMIT} bytes"
echo "Max heap: ${MAX_HEAP}MB"
echo "Initial heap: ${INIT_HEAP}MB"
echo "Starting with JAVA_OPTS: $JAVA_OPTS"

# Avvia l'applicazione
exec java $JAVA_OPTS -jar app.jar
