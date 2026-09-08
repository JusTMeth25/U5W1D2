package com.example.u5w1d2.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            tokens.utenteDi(token).ifPresent(username -> {
                var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(request, response);
    }
}
