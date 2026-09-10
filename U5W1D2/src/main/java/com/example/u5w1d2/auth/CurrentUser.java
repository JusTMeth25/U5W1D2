package com.example.u5w1d2.auth;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.exceptions.UnauthorizedException;
import com.example.u5w1d2.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Risolve l'utente autenticato a partire dal SecurityContext popolato dal
 * TokenAuthFilter. Nessuna autenticazione presente: 401.
 */
@Component
public class CurrentUser {

    private static final Logger log = LoggerFactory.getLogger(CurrentUser.class);

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public AppUser require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Spring Security mette comunque un token anonimo: senza questo controllo
        // il principal 'anonymousUser' verrebbe cercato tra gli utenti veri.
        if (auth == null || auth instanceof AnonymousAuthenticationToken
                || !(auth.getPrincipal() instanceof String username)) {
            log.warn("Accesso a risorsa protetta senza autenticazione");
            throw new UnauthorizedException();
        }
        return users.findByUsername(username).orElseGet(() -> {
            // Token valido ma utente sparito dal database: incoerenza, non un semplice 401 di routine.
            log.error("Token valido per '{}' ma l'utente non esiste piu' nel database", username);
            throw new UnauthorizedException();
        });
    }

    public String requireUsername() {
        return require().getUsername();
    }
}
