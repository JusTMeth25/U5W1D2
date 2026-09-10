package com.example.u5w1d2.notifications.dto;

import com.example.u5w1d2.entities.Message;
import com.example.u5w1d2.entities.Notification;

import java.time.Instant;

public record NotificationItem(
        Long id,
        String topic,
        String author,
        String preview,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationItem from(Notification n) {
        Message m = n.getMessage();
        String text = m.getText();
        String preview = text.length() > 120 ? text.substring(0, 120) + "…" : text;
        return new NotificationItem(
                n.getId(),
                m.getTopic().getName(),
                m.getAuthor().getUsername(),
                preview,
                m.getCreatedAt(),
                n.getReadAt());
    }
}
