package com.example.u5w1d2.messages.dto;

import com.example.u5w1d2.entities.Message;

import java.time.Instant;

public record MessageView(Long id, String topic, String author, String text, Instant createdAt) {

    public static MessageView from(Message m) {
        return new MessageView(
                m.getId(),
                m.getTopic().getName(),
                m.getAuthor().getUsername(),
                m.getText(),
                m.getCreatedAt());
    }
}
