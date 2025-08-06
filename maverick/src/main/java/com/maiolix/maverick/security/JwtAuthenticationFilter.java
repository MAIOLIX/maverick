package com.maiolix.maverick.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.maiolix.maverick.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro per l'autenticazione JWT
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (isValidJwt(jwt)) {
                processAuthentication(jwt);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se il JWT è valido
     */
    private boolean isValidJwt(String jwt) {
        return StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt);
    }

    /**
     * Processa l'autenticazione per un JWT valido
     */
    private void processAuthentication(String jwt) {
        String username = jwtTokenUtil.getUsernameFromToken(jwt);
        String userType = jwtTokenUtil.getUserType(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = getUserRole(jwt, userType);

            if (role != null) {
                setSecurityContext(username, role, jwt, userType);
                log.debug("Authenticated user '{}' with role '{}'", username, role);
            }
        }
    }

    /**
     * Imposta il contesto di sicurezza Spring Security
     */
    private void setSecurityContext(String username, String role, String jwt, String userType) {
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );

        JwtAuthenticationDetails details = createAuthDetails(jwt, userType);
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Crea i dettagli di autenticazione
     */
    private JwtAuthenticationDetails createAuthDetails(String jwt, String userType) {
        Long userId = jwtTokenUtil.isHumanToken(jwt) ? jwtTokenUtil.getUserIdFromToken(jwt) : null;
        Long clientId = jwtTokenUtil.isMachineToken(jwt) ? jwtTokenUtil.getClientIdFromToken(jwt) : null;
        
        return new JwtAuthenticationDetails(userType, userId, clientId);
    }

    /**
     * Estrae il token JWT dalla richiesta
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Ottiene il ruolo dell'utente dal database
     */
    private String getUserRole(String jwt, String userType) {
        try {
            if ("HUMAN".equals(userType)) {
                Long userId = jwtTokenUtil.getUserIdFromToken(jwt);
                return userService.getUserRole(userId);
            } else if ("MACHINE".equals(userType)) {
                Long clientId = jwtTokenUtil.getClientIdFromToken(jwt);
                return userService.getClientRole(clientId);
            }
        } catch (Exception e) {
            log.error("Error getting user role for token", e);
        }
        return null;
    }

    /**
     * Classe per contenere informazioni aggiuntive sull'autenticazione JWT
     */
    public static class JwtAuthenticationDetails {
        private final String userType;
        private final Long userId;
        private final Long clientId;

        public JwtAuthenticationDetails(String userType, Long userId, Long clientId) {
            this.userType = userType;
            this.userId = userId;
            this.clientId = clientId;
        }

        public String getUserType() {
            return userType;
        }

        public Long getUserId() {
            return userId;
        }

        public Long getClientId() {
            return clientId;
        }

        public boolean isHuman() {
            return "HUMAN".equals(userType);
        }

        public boolean isMachine() {
            return "MACHINE".equals(userType);
        }
    }
}
