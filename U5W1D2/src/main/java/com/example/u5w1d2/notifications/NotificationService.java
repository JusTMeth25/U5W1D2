package com.example.u5w1d2.notifications;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.exceptions.NotificationNotFoundException;
import com.example.u5w1d2.notifications.dto.NotificationItem;
import com.example.u5w1d2.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public Page<NotificationItem> list(AppUser current, Pageable pageable) {
        Page<NotificationItem> pagina = notifications.findByRecipientOrderByIdDesc(current, pageable)
                .map(NotificationItem::from);
        log.debug("Notifiche di '{}': pagina {} con {} elementi",
                current.getUsername(), pageable.getPageNumber(), pagina.getNumberOfElements());
        return pagina;
    }

    @Transactional(readOnly = true)
    public long unreadCount(AppUser current) {
        return notifications.countByRecipientAndReadAtIsNull(current);
    }

    /** Segna tutto come letto con un solo UPDATE. */
    @Transactional
    public void readAll(AppUser current) {
        int aggiornate = notifications.markAllRead(current, Instant.now());
        log.info("Notifiche marcate come lette per '{}': {}", current.getUsername(), aggiornate);
    }

    /**
     * Segna letta una singola notifica. Idempotente: rileggerla non e' un errore.
     * Id inesistente o di un altro utente danno lo stesso 404.
     */
    @Transactional
    public void read(Long id, AppUser current) {
        if (notifications.markRead(id, current, Instant.now()) == 1) {
            log.info("Notifica {} letta da '{}'", id, current.getUsername());
            return;
        }
        // Zero righe aggiornate: o era gia' letta, o non e' sua.
        if (notifications.findByIdAndRecipient(id, current).isEmpty()) {
            log.warn("Notifica {} non trovata per '{}'", id, current.getUsername());
            throw new NotificationNotFoundException(id);
        }
        log.debug("Notifica {} era gia' letta da '{}'", id, current.getUsername());
    }
}
