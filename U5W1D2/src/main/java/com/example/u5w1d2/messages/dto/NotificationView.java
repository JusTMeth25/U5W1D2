package com.example.u5w1d2.messages.dto;

import com.example.u5w1d2.entities.Message;

import java.time.Instant;

/** Riassunto che accende la campanella: bacheca, autore e anteprima del testo. */
public record NotificationView(Long messageId, String topic, String author, String preview, Instant createdAt) {

    public static NotificationView from(Message m) {
        String text = m.getText();
        String preview = text.length() > 120 ? text.substring(0, 120) + "…" : text;
        return new NotificationView(
                m.getId(),
                m.getTopic().getName(),
                m.getAuthor().getUsername(),
                preview,
                m.getCreatedAt());
    }
}
