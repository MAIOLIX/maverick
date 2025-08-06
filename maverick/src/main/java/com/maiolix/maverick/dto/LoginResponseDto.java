package com.maiolix.maverick.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la risposta di login utente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String status;
    private String message;
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String userType;
    private UserInfoDto user;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private Boolean isActive;
        private LocalDateTime lastLogin;
        private Long loginCount;
    }
}
