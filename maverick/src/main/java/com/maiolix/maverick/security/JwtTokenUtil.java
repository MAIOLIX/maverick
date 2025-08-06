package com.maiolix.maverick.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maiolix.maverick.entity.ApiClientEntity;
import com.maiolix.maverick.entity.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility per la gestione dei token JWT
 */
@Component
@Slf4j
public class JwtTokenUtil {

    private static final String USER_TYPE_CLAIM = "user_type";
    private static final String USER_TYPE_HUMAN = "HUMAN";
    private static final String USER_TYPE_MACHINE = "MACHINE";
    private static final String USER_ID_CLAIM = "user_id";
    private static final String CLIENT_ID_CLAIM = "client_id";
    private static final String EMAIL_CLAIM = "email";
    private static final String SCOPES_CLAIM = "scopes";
    private static final String RATE_LIMIT_CLAIM = "rate_limit";

    private final SecretKey key;
    private final long userTokenExpiration;
    private final long clientTokenExpiration;

    public JwtTokenUtil(@Value("${maverick.jwt.secret:maverick-super-secret-key-for-development-only-please-change-in-production}") String secret,
                        @Value("${maverick.jwt.user-expiration:900000}") long userTokenExpiration,  // 15 min
                        @Value("${maverick.jwt.client-expiration:86400000}") long clientTokenExpiration) {  // 24 ore
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.userTokenExpiration = userTokenExpiration;
        this.clientTokenExpiration = clientTokenExpiration;
    }

    /**
     * Crea token JWT per utente umano
     */
    public String createUserToken(UserEntity user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + userTokenExpiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(USER_TYPE_CLAIM, USER_TYPE_HUMAN)
                .claim(EMAIL_CLAIM, user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Crea token JWT per client API
     */
    public String createClientToken(ApiClientEntity client) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + clientTokenExpiration);

        return Jwts.builder()
                .subject(client.getClientId())
                .claim(CLIENT_ID_CLAIM, client.getId())
                .claim(USER_TYPE_CLAIM, USER_TYPE_MACHINE)
                .claim(SCOPES_CLAIM, client.getAllowedScopes())
                .claim(RATE_LIMIT_CLAIM, client.getRateLimitPerMinute())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Valida un token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Estrae username dal token
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Estrae user_id dal token (per utenti umani)
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get(USER_ID_CLAIM, Long.class);
    }

    /**
     * Estrae client_id dal token (per client API)
     */
    public Long getClientIdFromToken(String token) {
        return getClaimsFromToken(token).get(CLIENT_ID_CLAIM, Long.class);
    }

    /**
     * Estrae tipo utente dal token
     */
    public String getUserType(String token) {
        return getClaimsFromToken(token).get(USER_TYPE_CLAIM, String.class);
    }

    /**
     * Estrae email dal token
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get(EMAIL_CLAIM, String.class);
    }

    /**
     * Verifica se il token è scaduto
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Estrae data di scadenza dal token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * Estrae tutti i claims dal token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Ottiene il tempo rimanente del token in millisecondi
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Verifica se il token è di tipo umano
     */
    public boolean isHumanToken(String token) {
        return USER_TYPE_HUMAN.equals(getUserType(token));
    }

    /**
     * Verifica se il token è di tipo machine
     */
    public boolean isMachineToken(String token) {
        return USER_TYPE_MACHINE.equals(getUserType(token));
    }
}