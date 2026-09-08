package com.example.u5w1d2.exceptions;

public class NotSubscribedException extends RuntimeException {
    public NotSubscribedException(String topic) {
        super("Non iscritto al topic: " + topic);
    }
}
