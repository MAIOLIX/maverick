#!/bin/bash

# ================================
# MAVERICK VPS DEPLOYMENT SCRIPT
# ================================
# Script per deploy del container Maverick su VPS
# con PostgreSQL e MinIO esterni

set -e

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurazione
COMPOSE_FILE="docker-compose.vps.yml"
ENV_FILE=".env"
APP_NAME="maverick-app"

# Funzioni utility
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verifica prerequisiti
check_prerequisites() {
    log_info "Verifica prerequisiti..."
    
    # Verifica Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker non trovato. Installare Docker prima di continuare."
        exit 1
    fi
    
    # Verifica Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose non trovato. Installare Docker Compose prima di continuare."
        exit 1
    fi
    
    # Verifica file di configurazione
    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "File $COMPOSE_FILE non trovato!"
        exit 1
    fi
    
    if [ ! -f "$ENV_FILE" ]; then
        log_warning "File $ENV_FILE non trovato. Copiando da .env.example..."
        if [ -f ".env.example" ]; then
            cp .env.example .env
            log_warning "IMPORTANTE: Modifica il file .env con i tuoi parametri prima di continuare!"
            exit 1
        else
            log_error "File .env.example non trovato!"
            exit 1
        fi
    fi
    
    log_success "Prerequisiti verificati"
}

# Test connessioni esterne
test_external_services() {
    log_info "Test connessioni servizi esterni..."
    
    # Carica variabili environment
    source .env
    
    # Test PostgreSQL
    if [ -n "$DATABASE_URL" ]; then
        DB_HOST=$(echo $DATABASE_URL | sed 's/.*\/\/\([^:]*\).*/\1/')
        DB_PORT=$(echo $DATABASE_URL | sed 's/.*:\([0-9]*\)\/.*/\1/')
        
        log_info "Test connessione PostgreSQL: $DB_HOST:$DB_PORT"
        if timeout 5 bash -c "</dev/tcp/$DB_HOST/$DB_PORT"; then
            log_success "PostgreSQL raggiungibile"
        else
            log_warning "PostgreSQL non raggiungibile su $DB_HOST:$DB_PORT"
        fi
    fi
    
    # Test MinIO
    if [ -n "$MINIO_ENDPOINT" ]; then
        MINIO_HOST=$(echo $MINIO_ENDPOINT | sed 's/.*\/\/\([^:]*\).*/\1/')
        MINIO_PORT=$(echo $MINIO_ENDPOINT | sed 's/.*:\([0-9]*\).*/\1/')
        
        if [ "$MINIO_PORT" = "$MINIO_HOST" ]; then
            MINIO_PORT=9000  # Default port se non specificata
        fi
        
        log_info "Test connessione MinIO: $MINIO_HOST:$MINIO_PORT"
        if timeout 5 bash -c "</dev/tcp/$MINIO_HOST/$MINIO_PORT"; then
            log_success "MinIO raggiungibile"
        else
            log_warning "MinIO non raggiungibile su $MINIO_HOST:$MINIO_PORT"
        fi
    fi
}

# Build dell'immagine
build_image() {
    log_info "Build dell'immagine Maverick..."
    docker-compose -f $COMPOSE_FILE build --no-cache maverick
    log_success "Build completata"
}

# Deploy dell'applicazione
deploy() {
    log_info "Deploy dell'applicazione..."
    
    # Stop container esistente se presente
    if [ "$(docker ps -q -f name=$APP_NAME)" ]; then
        log_info "Stopping container esistente..."
        docker-compose -f $COMPOSE_FILE down
    fi
    
    # Avvio nuovo container
    docker-compose -f $COMPOSE_FILE up -d
    
    log_success "Deploy completato"
}

# Verifica health dell'applicazione
check_health() {
    log_info "Verifica health dell'applicazione..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec $APP_NAME curl -f http://localhost:8080/actuator/health &> /dev/null; then
            log_success "Applicazione healthy!"
            return 0
        fi
        
        log_info "Tentativo $attempt/$max_attempts - Attendo health check..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Applicazione non healthy dopo $max_attempts tentativi"
    return 1
}

# Mostra logs
show_logs() {
    log_info "Logs dell'applicazione:"
    docker-compose -f $COMPOSE_FILE logs -f maverick
}

# Mostra status
show_status() {
    log_info "Status dell'applicazione:"
    docker-compose -f $COMPOSE_FILE ps
    
    if [ "$(docker ps -q -f name=$APP_NAME)" ]; then
        log_info "Health status:"
        docker exec $APP_NAME curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || echo "Health endpoint non disponibile"
    fi
}

# Stop dell'applicazione
stop() {
    log_info "Stop dell'applicazione..."
    docker-compose -f $COMPOSE_FILE down
    log_success "Applicazione fermata"
}

# Restart dell'applicazione
restart() {
    log_info "Restart dell'applicazione..."
    stop
    deploy
}

# Cleanup delle risorse
cleanup() {
    log_info "Cleanup delle risorse..."
    
    # Stop e rimozione container
    docker-compose -f $COMPOSE_FILE down --volumes --remove-orphans
    
    # Rimozione immagini non utilizzate
    docker image prune -f
    
    log_success "Cleanup completato"
}

# Menu principale
show_usage() {
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandi disponibili:"
    echo "  check      - Verifica prerequisiti e connessioni"
    echo "  build      - Build dell'immagine Docker"
    echo "  deploy     - Deploy dell'applicazione (build + start)"
    echo "  start      - Avvia l'applicazione"
    echo "  stop       - Ferma l'applicazione"
    echo "  restart    - Riavvia l'applicazione"
    echo "  status     - Mostra status dell'applicazione"
    echo "  logs       - Mostra logs dell'applicazione"
    echo "  health     - Verifica health dell'applicazione"
    echo "  cleanup    - Cleanup di container e immagini"
    echo ""
    echo "Esempi:"
    echo "  $0 deploy    # Deploy completo"
    echo "  $0 logs      # Mostra logs in tempo reale"
    echo "  $0 status    # Mostra status container"
}

# Main
main() {
    case "${1:-}" in
        "check")
            check_prerequisites
            test_external_services
            ;;
        "build")
            check_prerequisites
            build_image
            ;;
        "deploy")
            check_prerequisites
            test_external_services
            build_image
            deploy
            check_health
            ;;
        "start")
            check_prerequisites
            docker-compose -f $COMPOSE_FILE up -d
            log_success "Applicazione avviata"
            ;;
        "stop")
            stop
            ;;
        "restart")
            restart
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "health")
            check_health
            ;;
        "cleanup")
            cleanup
            ;;
        *)
            show_usage
            exit 1
            ;;
    esac
}

main "$@"
