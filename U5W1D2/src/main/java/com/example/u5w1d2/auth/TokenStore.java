package com.example.u5w1d2.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token opaco tenuto in memoria: mappa token -> username.
 * Non persiste: al riavvio del backend tutti i token decadono.
 */
@Component
public class TokenStore {

    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public String issue(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUsername.put(token, username);
        return token;
    }

    /** Ritorna lo username associato al token, o vuoto se il token non esiste. */
    public Optional<String> utenteDi(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(tokenToUsername.get(token));
    }
}
