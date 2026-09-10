package com.example.u5w1d2.exceptions;

/**
 * Notifica inesistente, oppure esistente ma di un altro utente: al chiamante
 * arriva lo stesso 404, cosi' non puo' scoprire quali id esistono.
 */
public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long id) {
        super("Notifica non trovata: " + id);
    }
}
