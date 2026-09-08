package com.example.u5w1d2.exceptions;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String username) {
        super("Username gia' in uso: " + username);
    }
}
