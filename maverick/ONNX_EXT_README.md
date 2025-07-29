# OnnxExtModelHandler - Guida d'uso

## Descrizione
`OnnxExtModelHandler` è un handler avanzato per modelli ONNX che supporta la mappatura automatica delle classi tramite un file ZIP contenente:
1. Il file del modello ONNX (`.onnx`)
2. Un file JSON con la mappatura delle classi (preferibilmente `labels.json`)

## Formato del ZIP
Il file ZIP deve contenere:
```
model.zip
├── iris_model.onnx          # Il modello ONNX
└── labels.json              # La mappatura delle classi
```

## Formato del file JSON labels
Il file JSON deve contenere la mappatura da indice numerico a nome della classe:

```json
{
  "0": "setosa",
  "1": "versicolor", 
  "2": "virginica"
}
```

Oppure con chiavi numeriche:
```json
{
  0: "setosa",
  1: "versicolor",
  2: "virginica"
}
```

## Come utilizzare

### 1. Upload del modello
```bash
POST /models/upload
Content-Type: multipart/form-data

- file: model.zip
- modelName: iris-model
- type: ONNX_EXT
- version: 1.0
```

### 2. Predizione
```bash
POST /models/predict/iris-model/1.0
Content-Type: application/json

{
  "sepal_length": 5.1,
  "sepal_width": 3.5,
  "petal_length": 1.4,
  "petal_width": 0.2
}
```

### 3. Risposta arricchita
La risposta includerà automaticamente le informazioni delle classi:

```json
{
  "output_label": [0],
  "output_probability": [...],
  "labelMapping": {
    "0": "setosa",
    "1": "versicolor",
    "2": "virginica"
  },
  "predictedClassName": "setosa",
  "classProbabilities": {
    "setosa": 0.9999193,
    "versicolor": 7.711729E-5,
    "virginica": 3.6218382E-6
  }
}
```

## Vantaggi

1. **Mappatura automatica**: Le label numeriche vengono automaticamente convertite in nomi leggibili
2. **Gestione ZIP**: Supporta file ZIP per pacchettizzare modello e metadati
3. **Flessibilità**: Accetta sia chiavi stringa che numeriche nel JSON
4. **Compatibilità**: Mantiene l'output originale e aggiunge informazioni arricchite
5. **Robustezza**: Gestione degli errori completa con logging dettagliato

## Tipi di modello supportati
- `ONNX`: Modello ONNX singolo (handler originale)
- `ONNX_EXT`: Modello ONNX + mappatura classi in ZIP (nuovo handler)
- `MOJO`: Modelli H2O MOJO
- `PMML`: Modelli PMML
