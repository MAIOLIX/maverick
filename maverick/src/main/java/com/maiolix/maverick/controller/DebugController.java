package com.maiolix.maverick.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller di debug per testare hash delle password
 * DA RIMUOVERE IN PRODUZIONE!
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final PasswordEncoder passwordEncoder;

    /**
     * Genera hash BCrypt per una password
     */
    @PostMapping("/hash-password")
    public Map<String, String> hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        return result;
    }

    /**
     * Verifica se una password corrisponde a un hash
     */
    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", matches);
        return result;
    }

    /**
     * Test password predefinite
     */
    @GetMapping("/test-passwords")
    public Map<String, Object> testPasswords() {
        Map<String, Object> result = new HashMap<>();
        
        // Test password "password"
        String passwordHash = passwordEncoder.encode("password");
        boolean passwordMatches = passwordEncoder.matches("password", passwordHash);
        
        // Test password "test123"
        String test123Hash = passwordEncoder.encode("test123");
        boolean test123Matches = passwordEncoder.matches("test123", test123Hash);
        
        // Test password "admin123"
        String admin123Hash = passwordEncoder.encode("admin123");
        boolean admin123Matches = passwordEncoder.matches("admin123", admin123Hash);
        
        result.put("password_test", Map.of(
            "password", "password",
            "hash", passwordHash,
            "matches", passwordMatches
        ));
        
        result.put("test123_test", Map.of(
            "password", "test123", 
            "hash", test123Hash,
            "matches", test123Matches
        ));
        
        result.put("admin123_test", Map.of(
            "password", "admin123",
            "hash", admin123Hash, 
            "matches", admin123Matches
        ));
        
        return result;
    }
}
