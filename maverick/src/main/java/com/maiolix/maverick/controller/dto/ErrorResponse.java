package com.maiolix.maverick.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String error;
    private String message;
    private String path;
    private int status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String details;
    
    public static ErrorResponse of(String error, String message, String path, int status) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .path(path)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(String error, String message, String path, int status, String details) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .path(path)
                .status(status)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}
