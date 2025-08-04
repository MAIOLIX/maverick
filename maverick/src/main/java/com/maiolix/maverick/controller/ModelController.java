package com.maiolix.maverick.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maiolix.maverick.service.IModelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Model Management", description = "API per la gestione e predizione di modelli ML (MOJO, ONNX, PMML)")
public class ModelController {

    private static final String VERSION_SEPARATOR = " version: ";
    
    private final IModelService modelService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload modello ML", 
               description = "Carica un nuovo modello di Machine Learning nel sistema. Supporta MOJO, ONNX e PMML.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Modello caricato con successo",
                    content = @Content(mediaType = "text/plain", 
                                     examples = @ExampleObject(value = "Model uploaded successfully: iris-model version: 1.0"))),
        @ApiResponse(responseCode = "400", 
                    description = "Errore nei parametri di input"),
        @ApiResponse(responseCode = "500", 
                    description = "Errore durante l'upload del modello")
    })
    public ResponseEntity<String> uploadModel(
            @Parameter(description = "File del modello da caricare", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @RequestParam("modelName") String modelName,
            @Parameter(description = "Tipo di modello", schema = @Schema(allowableValues = {"MOJO", "ONNX", "PMML"}), required = true)
            @RequestParam("type") String type,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @RequestParam("version") String version) {
        
        modelService.uploadModel(file, modelName, type, version);
        log.info("Model uploaded successfully: {} version: {}", modelName, version);
        return ResponseEntity.ok("Model uploaded successfully: " + modelName + VERSION_SEPARATOR + version);
    }

    @PostMapping("/predict/{version}/{modelName}")
    @Operation(summary = "Esegui predizione", 
               description = "Esegue una predizione utilizzando il modello specificato.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Predizione eseguita con successo",
                    content = @Content(mediaType = "application/json",
                                     examples = @ExampleObject(value = "{\"prediction\": 0.85, \"probability\": [0.15, 0.85]}"))),
        @ApiResponse(responseCode = "400", 
                    description = "Input non valido per il modello"),
        @ApiResponse(responseCode = "404", 
                    description = "Modello non trovato"),
        @ApiResponse(responseCode = "500", 
                    description = "Errore durante la predizione")
    })
    public ResponseEntity<Object> predict(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @PathVariable String modelName,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @PathVariable String version,
            @Parameter(description = "Dati di input per la predizione", required = true,
                      content = @Content(examples = @ExampleObject(value = "{\"sepal_length\": 5.1, \"sepal_width\": 3.5, \"petal_length\": 1.4, \"petal_width\": 0.2}")))
            @RequestBody Object input) {
        
        Object prediction = modelService.predict(modelName, version, input);
        log.debug("Prediction completed for model: {} version: {}", modelName, version);
        return ResponseEntity.ok(prediction);
    }
    
    @GetMapping("/schema/{version}/{modelName}")
    @Operation(summary = "Ottieni schema input", 
               description = "Restituisce lo schema dei dati di input richiesti dal modello.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Schema recuperato con successo",
                    content = @Content(mediaType = "application/json",
                                     examples = @ExampleObject(value = "{\"inputs\": {\"sepal_length\": \"float\", \"sepal_width\": \"float\"}, \"modelType\": \"ONNX\"}"))),
        @ApiResponse(responseCode = "404", 
                    description = "Modello non trovato")
    })
    public ResponseEntity<Object> getInputSchema(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @PathVariable String modelName,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @PathVariable String version) {
        
        Object schema = modelService.getInputSchema(modelName, version);
        log.debug("Input schema retrieved for model: {} version: {}", modelName, version);
        return ResponseEntity.ok(schema);
    }
    
    @GetMapping("/info/{modelName}")
    @Operation(summary = "Ottieni tutti i modelli per nome", 
               description = "Restituisce tutte le versioni disponibili di un modello specificato per nome.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Modelli recuperati con successo",
                    content = @Content(mediaType = "application/json",
                                     examples = @ExampleObject(value = "[{\"modelName\": \"iris-model\", \"version\": \"1.0\", \"type\": \"ONNX\"}, {\"modelName\": \"iris-model\", \"version\": \"2.0\", \"type\": \"ONNX\"}]"))),
        @ApiResponse(responseCode = "404", 
                    description = "Nessun modello trovato con questo nome")
    })
    public ResponseEntity<Object> getModelsByName(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @PathVariable String modelName) {
        
        Object models = modelService.getModelsByName(modelName);
        log.debug("Retrieved models for name: {}", modelName);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/info/{version}/{modelName}")
    @Operation(summary = "Ottieni informazioni modello", 
               description = "Restituisce informazioni dettagliate sul modello specificato.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Informazioni recuperate con successo",
                    content = @Content(mediaType = "application/json",
                                     examples = @ExampleObject(value = "{\"modelName\": \"iris-model\", \"version\": \"1.0\", \"type\": \"ONNX\", \"uploadDate\": \"2025-08-04\"}"))),
        @ApiResponse(responseCode = "404", 
                    description = "Modello non trovato")
    })
    public ResponseEntity<Object> getModelInfo(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @PathVariable String modelName,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @PathVariable String version) {
        
        Object info = modelService.getModelInfo(modelName, version);
        log.debug("Model info retrieved for model: {} version: {}", modelName, version);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/add")
    @Operation(summary = "Aggiungi modello alla cache", 
               description = "Aggiunge un modello già esistente alla cache in memoria.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Modello aggiunto alla cache con successo",
                    content = @Content(mediaType = "text/plain", 
                                     examples = @ExampleObject(value = "Model added successfully: iris-model version: 1.0"))),
        @ApiResponse(responseCode = "400", 
                    description = "Errore nei parametri di input"),
        @ApiResponse(responseCode = "500", 
                    description = "Errore durante l'aggiunta del modello")
    })
    public ResponseEntity<String> addModel(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @RequestParam("modelName") String modelName,
            @Parameter(description = "Tipo di modello", schema = @Schema(allowableValues = {"MOJO", "ONNX", "PMML"}), required = true)
            @RequestParam("type") String type,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @RequestParam("version") String version,
            @Parameter(description = "Handler del modello", required = true)
            @RequestBody Object handler) {
        
        modelService.addModel(modelName, type, version, handler);
        log.info("Model added successfully: {} version: {}", modelName, version);
        return ResponseEntity.ok("Model added successfully: " + modelName + VERSION_SEPARATOR + version);
    }

    @DeleteMapping("/{version}/{modelName}")
    @Operation(summary = "Rimuovi modello", 
               description = "Rimuove un modello dalla cache in memoria.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Modello rimosso con successo o non trovato",
                    content = @Content(mediaType = "text/plain", 
                                     examples = {
                                         @ExampleObject(name = "success", value = "Model removed successfully: iris-model version: 1.0"),
                                         @ExampleObject(name = "not-found", value = "Model not found: iris-model version: 1.0")
                                     }))
    })
    public ResponseEntity<String> removeModel(
            @Parameter(description = "Nome del modello", example = "iris-model", required = true)
            @PathVariable String modelName,
            @Parameter(description = "Versione del modello", example = "1.0", required = true)
            @PathVariable String version) {
        
        boolean removed = modelService.removeModel(modelName, version);
        if (removed) {
            log.info("Model removed successfully: {} version: {}", modelName, version);
            return ResponseEntity.ok("Model removed successfully: " + modelName + VERSION_SEPARATOR + version);
        } else {
            log.warn("Model not found for removal: {} version: {}", modelName, version);
            return ResponseEntity.ok("Model not found: " + modelName + VERSION_SEPARATOR + version);
        }
    }

    @GetMapping("/list")
    @Operation(summary = "Lista tutti i modelli", 
               description = "Restituisce la lista di tutti i modelli attualmente presenti nella cache.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", 
                    description = "Lista modelli recuperata con successo",
                    content = @Content(mediaType = "application/json",
                                     examples = @ExampleObject(value = "[{\"modelName\": \"iris-model\", \"version\": \"1.0\", \"type\": \"ONNX\"}, {\"modelName\": \"fraud-detection\", \"version\": \"2.1\", \"type\": \"MOJO\"}]")))
    })
    public ResponseEntity<Object> getAllModels() {
        Object models = modelService.getAllModels();
        log.debug("Retrieved all models from cache");
        return ResponseEntity.ok(models);
    }
}