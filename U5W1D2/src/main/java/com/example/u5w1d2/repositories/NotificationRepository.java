package com.example.u5w1d2.repositories;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderByIdDesc(AppUser recipient, Pageable pageable);

    /** Contatore delle non lette: usa l'indice (recipient_id, read_at). */
    long countByRecipientAndReadAtIsNull(AppUser recipient);

    /** Segna tutte le non lette dell'utente come lette con un solo UPDATE. */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.recipient = :recipient AND n.readAt IS NULL")
    int markAllRead(@Param("recipient") AppUser recipient, @Param("now") Instant now);
}
