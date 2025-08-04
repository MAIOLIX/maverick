package com.maiolix.maverick.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.maiolix.maverick.controller.dto.ErrorResponse;
import com.maiolix.maverick.exception.ModelNotFoundException;
import com.maiolix.maverick.exception.ModelPredictionException;
import com.maiolix.maverick.exception.ModelUploadException;
import com.maiolix.maverick.exception.MojoModelException;
import com.maiolix.maverick.exception.MojoPredictionException;
import com.maiolix.maverick.exception.OnnxExtModelException;
import com.maiolix.maverick.exception.OnnxExtPredictionException;
import com.maiolix.maverick.exception.OnnxModelException;
import com.maiolix.maverick.exception.OnnxPredictionException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ModelNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleModelNotFoundException(
            ModelNotFoundException ex, HttpServletRequest request) {
        
        log.error("Model not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
                "MODEL_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({ModelUploadException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleModelUploadException(
            ModelUploadException ex, HttpServletRequest request) {
        
        log.error("Model upload error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
                "MODEL_UPLOAD_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                "Check your model file format and parameters"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({ModelPredictionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleModelPredictionException(
            ModelPredictionException ex, HttpServletRequest request) {
        
        log.error("Model prediction error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
                "MODEL_PREDICTION_ERROR",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                "Check your input data format and model availability"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({
        OnnxModelException.class,
        OnnxExtModelException.class,
        MojoModelException.class
    })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorResponse> handleModelFormatException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("Model format error: {}", ex.getMessage());
        
        String errorCode = "MODEL_FORMAT_ERROR";
        if (ex instanceof OnnxModelException) {
            errorCode = "ONNX_MODEL_ERROR";
        } else if (ex instanceof OnnxExtModelException) {
            errorCode = "ONNX_EXT_MODEL_ERROR";
        } else if (ex instanceof MojoModelException) {
            errorCode = "MOJO_MODEL_ERROR";
        }
        
        ErrorResponse error = ErrorResponse.of(
                errorCode,
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "The uploaded model file format is not valid or corrupted"
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler({
        OnnxPredictionException.class,
        OnnxExtPredictionException.class,
        MojoPredictionException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handlePredictionFormatException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.error("Prediction format error: {}", ex.getMessage());
        
        String errorCode = "PREDICTION_FORMAT_ERROR";
        if (ex instanceof OnnxPredictionException) {
            errorCode = "ONNX_PREDICTION_ERROR";
        } else if (ex instanceof OnnxExtPredictionException) {
            errorCode = "ONNX_EXT_PREDICTION_ERROR";
        } else if (ex instanceof MojoPredictionException) {
            errorCode = "MOJO_PREDICTION_ERROR";
        }
        
        ErrorResponse error = ErrorResponse.of(
                errorCode,
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                "Check the input data format required by the model"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        log.error("File upload size exceeded: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
                "FILE_SIZE_EXCEEDED",
                "The uploaded file size exceeds the maximum allowed limit",
                request.getRequestURI(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Maximum file size allowed is configured in application properties"
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        StringBuilder details = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            details.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        
        ErrorResponse error = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                details.toString()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.error("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                "Check the provided parameters and their format"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        log.warn("Resource not found: {} - URI: {}", ex.getMessage(), request.getRequestURI());
        
        // Check if it's an API call to wrong endpoint
        String uri = request.getRequestURI();
        String correctedEndpoint = "";
        
        if (uri.contains("/models/upload")) {
            correctedEndpoint = "Use '/api/v1/models/upload' instead";
        } else if (uri.contains("/models/predict")) {
            correctedEndpoint = "Use '/api/v1/models/predict/{version}/{modelName}' instead";
        } else if (uri.contains("/models/")) {
            correctedEndpoint = "API endpoints are under '/api/v1/models/'";
        }
        
        ErrorResponse error = ErrorResponse.of(
                "ENDPOINT_NOT_FOUND",
                "The requested endpoint does not exist: " + uri,
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value(),
                correctedEndpoint.isEmpty() ? "Check the API documentation for correct endpoints" : correctedEndpoint
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Please contact support if the problem persists"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
