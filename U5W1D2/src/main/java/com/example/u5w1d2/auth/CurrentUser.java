package com.example.u5w1d2.auth;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.exceptions.UnauthorizedException;
import com.example.u5w1d2.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Risolve l'utente autenticato a partire dal SecurityContext popolato dal
 * TokenAuthFilter. Nessuna autenticazione presente: 401.
 */
@Component
public class CurrentUser {

    private final UserRepository users;

    public CurrentUser(UserRepository users) {
        this.users = users;
    }

    public AppUser require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String username)) {
            throw new UnauthorizedException();
        }
        return users.findByUsername(username).orElseThrow(UnauthorizedException::new);
    }

    public String requireUsername() {
        return require().getUsername();
    }
}
