#!/bin/bash

# Script di test per i nuovi endpoint di schema dei modelli
# Assicurati che l'applicazione Maverick sia in esecuzione su localhost:8080

BASE_URL="http://localhost:8080/api/v1/maverick"
MODEL_NAME="iris-classifier"
VERSION="v1.0"

echo "🚀 Test degli Endpoint di Schema Modelli"
echo "========================================"
echo

echo "📋 1. Test Schema Input Specifico"
echo "GET $BASE_URL/models/$MODEL_NAME/versions/$VERSION/input-schema"
echo
curl -s -X GET "$BASE_URL/models/$MODEL_NAME/versions/$VERSION/input-schema" | \
  python -m json.tool 2>/dev/null || echo "Modello non trovato o server non raggiungibile"
echo
echo

echo "ℹ️ 2. Test Informazioni Complete Modello"
echo "GET $BASE_URL/models/$MODEL_NAME/versions/$VERSION/info"
echo
curl -s -X GET "$BASE_URL/models/$MODEL_NAME/versions/$VERSION/info" | \
  python -m json.tool 2>/dev/null || echo "Modello non trovato o server non raggiungibile"
echo
echo

echo "📊 3. Test Schema di Tutti i Modelli"
echo "GET $BASE_URL/models/schemas"
echo
curl -s -X GET "$BASE_URL/models/schemas" | \
  python -m json.tool 2>/dev/null || echo "Server non raggiungibile"
echo
echo

echo "🔍 4. Test Modello Non Esistente (404 atteso)"
echo "GET $BASE_URL/models/non-esistente/versions/v1.0/input-schema"
echo
curl -s -X GET "$BASE_URL/models/non-esistente/versions/v1.0/input-schema" | \
  python -m json.tool 2>/dev/null || echo "404 - Modello non trovato (come atteso)"
echo
echo

echo "✅ Test completati!"
echo
echo "💡 Suggerimenti:"
echo "   - Assicurati che l'applicazione sia in esecuzione"
echo "   - Verifica che ci siano modelli caricati in memoria"
echo "   - Usa 'GET /api/v1/maverick/models-in-memory' per vedere i modelli disponibili"
