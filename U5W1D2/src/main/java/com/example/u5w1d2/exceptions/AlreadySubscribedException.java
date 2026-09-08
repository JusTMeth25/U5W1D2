package com.example.u5w1d2.exceptions;

public class AlreadySubscribedException extends RuntimeException {
    public AlreadySubscribedException(String topic) {
        super("Gia' iscritto al topic: " + topic);
    }
}
