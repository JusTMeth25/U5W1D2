package com.example.u5w1d2.exceptions;

public class TopicNotFoundException extends RuntimeException {
    public TopicNotFoundException(String name) {
        super("Topic non trovato: " + name);
    }
}
