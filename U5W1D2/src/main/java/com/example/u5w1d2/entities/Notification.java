package com.example.u5w1d2.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "notifications",
        indexes = @Index(name = "idx_notifications_recipient_read", columnList = "recipient_id, read_at")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AppUser recipient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "read_at")
    private Instant readAt;

    public Notification(AppUser recipient, Message message) {
        this.recipient = recipient;
        this.message = message;
    }
}
