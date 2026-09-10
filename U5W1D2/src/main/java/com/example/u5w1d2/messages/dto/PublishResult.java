package com.example.u5w1d2.messages.dto;

import java.util.List;

/**
 * Esito di publish: il messaggio salvato e una notifica gia' indirizzata per ogni
 * destinatario. Cosi' il controller invia i frame senza rifare query.
 */
public record PublishResult(MessageView messaggio, List<NotificaDestinata> notifiche) {
}
