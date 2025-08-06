package com.maiolix.maverick.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di token client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {

    private String status;
    private String message;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String userType;
    private String clientId;
    private String role;
    private List<String> scopes;
    private Integer rateLimitPerMinute;
    private LocalDateTime timestamp;
}
