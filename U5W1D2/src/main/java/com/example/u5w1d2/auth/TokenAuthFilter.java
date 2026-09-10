package com.example.u5w1d2.auth;

import com.example.u5w1d2.logging.LoggingContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Legge l'header Authorization: Bearer <token>, traduce il token in username
 * tramite TokenStore e popola il SecurityContext. Token assente o non valido:
 * nessuna autenticazione, la richiesta prosegue anonima (i controller
 * protetti risponderanno 401 via CurrentUser).
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthFilter.class);

    private final TokenStore tokens;

    public TokenAuthFilter(TokenStore tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokens.utenteDi(token).ifPresentOrElse(username -> {
                var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Da qui in avanti ogni riga di log della richiesta riporta l'utente.
                LoggingContext.setUser(username);
                log.debug("Richiesta autenticata come '{}'", username);
            }, () -> log.warn("Token non riconosciuto: la richiesta prosegue anonima"));
        }
        chain.doFilter(request, response);
    }
}
