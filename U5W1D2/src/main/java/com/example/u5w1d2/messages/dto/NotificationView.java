package com.example.u5w1d2.messages.dto;

import com.example.u5w1d2.entities.Message;
import com.example.u5w1d2.entities.Notification;

import java.time.Instant;

/**
 * Riassunto che accende la campanella: bacheca, autore e anteprima del testo.
 * <p>
 * Porta anche l'id della riga notifications, che e' diverso per ogni destinatario:
 * senza quello il client non potrebbe segnare letta la singola notifica arrivata
 * via WebSocket, ma solo quelle ricaricate dall'elenco.
 */
public record NotificationView(Long id, Long messageId, String topic, String author, String preview,
                               Instant createdAt) {

    public static NotificationView from(Notification n) {
        Message m = n.getMessage();
        String text = m.getText();
        String preview = text.length() > 120 ? text.substring(0, 120) + "…" : text;
        return new NotificationView(
                n.getId(),
                m.getId(),
                m.getTopic().getName(),
                m.getAuthor().getUsername(),
                preview,
                m.getCreatedAt());
    }
}
