#!/bin/bash

# Script di build e deploy per Maverick Platform Docker

set -e

echo "🚀 Maverick Platform - Docker Build & Deploy"
echo "=============================================="

# Variabili
IMAGE_NAME="maverick-platform"
IMAGE_TAG="latest"
CONTAINER_NAME="maverick-app"

# Funzioni
build_image() {
    echo "📦 Building Docker image..."
    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
    echo "✅ Image built successfully!"
}

start_services() {
    echo "🔄 Starting services with Docker Compose..."
    docker-compose up -d
    echo "✅ Services started!"
}

stop_services() {
    echo "⏹️ Stopping services..."
    docker-compose down
    echo "✅ Services stopped!"
}

check_health() {
    echo "🔍 Checking health status..."
    
    # Attendi che i servizi siano pronti
    echo "Waiting for PostgreSQL..."
    while ! docker-compose exec postgres pg_isready -U maverick -d maverickDB > /dev/null 2>&1; do
        sleep 2
    done
    
    echo "Waiting for MinIO..."
    while ! curl -s http://localhost:9000/minio/health/live > /dev/null 2>&1; do
        sleep 2
    done
    
    echo "Waiting for Maverick App..."
    while ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; do
        sleep 5
    done
    
    echo "✅ All services are healthy!"
    echo ""
    echo "🌐 Service URLs:"
    echo "   - Maverick API: http://localhost:8080"
    echo "   - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "   - MinIO Console: http://localhost:9001 (admin/minioadmin123)"
    echo "   - PostgreSQL: localhost:5432 (maverick/Alessandro12)"
}

show_logs() {
    echo "📋 Showing application logs..."
    docker-compose logs -f maverick-app
}

cleanup() {
    echo "🧹 Cleaning up..."
    docker-compose down -v
    docker system prune -f
    echo "✅ Cleanup completed!"
}

# Menu principale
case "${1:-help}" in
    "build")
        build_image
        ;;
    "start")
        start_services
        check_health
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        start_services
        check_health
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
    "deploy")
        build_image
        start_services
        check_health
        ;;
    "help"|*)
        echo "Usage: $0 {build|start|stop|restart|logs|health|cleanup|deploy}"
        echo ""
        echo "Commands:"
        echo "  build    - Build Docker image"
        echo "  start    - Start all services"
        echo "  stop     - Stop all services"
        echo "  restart  - Restart all services"
        echo "  logs     - Show application logs"
        echo "  health   - Check service health"
        echo "  cleanup  - Stop services and clean up"
        echo "  deploy   - Build and deploy everything"
        ;;
esac
