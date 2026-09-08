package com.example.u5w1d2.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Autenticazione richiesta");
    }
}
