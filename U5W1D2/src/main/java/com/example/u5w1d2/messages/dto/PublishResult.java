package com.example.u5w1d2.messages.dto;

import java.util.List;

/**
 * Esito di publish: il messaggio salvato, gli username destinatari e il
 * riassunto notifica. Cosi' il controller invia i frame senza rifare query.
 */
public record PublishResult(MessageView messaggio, List<String> destinatari, NotificationView notifica) {
}
