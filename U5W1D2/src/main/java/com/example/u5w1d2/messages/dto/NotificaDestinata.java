package com.example.u5w1d2.messages.dto;

/**
 * Una notifica insieme all'utente a cui va consegnata. La pubblicazione ne crea
 * una per ogni iscritto: il contenuto e' lo stesso, l'id della riga notifications no.
 */
public record NotificaDestinata(String username, NotificationView notifica) {
}
