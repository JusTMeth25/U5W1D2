package com.example.u5w1d2.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenziali non valide");
    }
}
