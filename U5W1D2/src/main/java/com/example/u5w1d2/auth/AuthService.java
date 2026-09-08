package com.example.u5w1d2.auth;

import com.example.u5w1d2.auth.dto.AuthRequest;
import com.example.u5w1d2.auth.dto.AuthResponse;
import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.exceptions.InvalidCredentialsException;
import com.example.u5w1d2.exceptions.UsernameTakenException;
import com.example.u5w1d2.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final TokenStore tokens;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, TokenStore tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public AuthResponse register(AuthRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new UsernameTakenException(req.username());
        }
        AppUser user = new AppUser(req.username(), encoder.encode(req.password()));
        users.save(user);
        return new AuthResponse(user.getUsername(), tokens.issue(user.getUsername()));
    }

    public AuthResponse login(AuthRequest req) {
        AppUser user = users.findByUsername(req.username())
                .orElseThrow(InvalidCredentialsException::new);
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(user.getUsername(), tokens.issue(user.getUsername()));
    }
}
