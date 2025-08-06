package com.maiolix.maverick.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility per generare hash BCrypt per le password di test
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        // Password per admin
        String adminPassword = "admin123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin password hash:");
        System.out.println(adminHash);
        System.out.println();
        
        // Password per predictor
        String predictorPassword = "predictor123";
        String predictorHash = encoder.encode(predictorPassword);
        System.out.println("Predictor password hash:");
        System.out.println(predictorHash);
        System.out.println();
        
        // Client secrets
        String adminSecret = "admin-secret-2024";
        String adminSecretHash = encoder.encode(adminSecret);
        System.out.println("Admin client secret hash:");
        System.out.println(adminSecretHash);
        System.out.println();
        
        String predictorSecret = "predictor-secret-2024";
        String predictorSecretHash = encoder.encode(predictorSecret);
        System.out.println("Predictor client secret hash:");
        System.out.println(predictorSecretHash);
        System.out.println();
        
        // Verifica password
        System.out.println("Verification:");
        System.out.println("Admin password matches: " + encoder.matches(adminPassword, adminHash));
        System.out.println("Predictor password matches: " + encoder.matches(predictorPassword, predictorHash));
        System.out.println("Admin secret matches: " + encoder.matches(adminSecret, adminSecretHash));
        System.out.println("Predictor secret matches: " + encoder.matches(predictorSecret, predictorSecretHash));
    }
}
