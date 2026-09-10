package com.example.u5w1d2.auth;

import com.example.u5w1d2.auth.dto.AuthRequest;
import com.example.u5w1d2.auth.dto.AuthResponse;
import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.exceptions.InvalidCredentialsException;
import com.example.u5w1d2.exceptions.UsernameTakenException;
import com.example.u5w1d2.logging.LoggingContext;
import com.example.u5w1d2.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final TokenStore tokens;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, TokenStore tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public AuthResponse register(AuthRequest req) {
        if (users.existsByUsername(req.username())) {
            log.warn("Registrazione rifiutata: username '{}' gia' in uso", req.username());
            throw new UsernameTakenException(req.username());
        }
        AppUser user = new AppUser(req.username(), encoder.encode(req.password()));
        users.save(user);
        LoggingContext.setUser(user.getUsername());
        log.info("Nuovo utente registrato: '{}' (id {})", user.getUsername(), user.getId());
        // Il token non finisce mai nel log: e' una credenziale.
        return new AuthResponse(user.getUsername(), tokens.issue(user.getUsername()));
    }

    public AuthResponse login(AuthRequest req) {
        AppUser user = users.findByUsername(req.username())
                .orElseGet(() -> {
                    log.warn("Login fallito: utente '{}' inesistente", req.username());
                    throw new InvalidCredentialsException();
                });
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            log.warn("Login fallito: password errata per '{}'", user.getUsername());
            throw new InvalidCredentialsException();
        }
        LoggingContext.setUser(user.getUsername());
        log.info("Login riuscito per '{}'", user.getUsername());
        return new AuthResponse(user.getUsername(), tokens.issue(user.getUsername()));
    }
}
