package com.example.u5w1d2.messages;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Message;
import com.example.u5w1d2.entities.Notification;
import com.example.u5w1d2.entities.Topic;
import com.example.u5w1d2.exceptions.NotSubscribedException;
import com.example.u5w1d2.exceptions.TopicNotFoundException;
import com.example.u5w1d2.messages.dto.MessageView;
import com.example.u5w1d2.messages.dto.NotificationView;
import com.example.u5w1d2.messages.dto.PublishResult;
import com.example.u5w1d2.repositories.MessageRepository;
import com.example.u5w1d2.repositories.NotificationRepository;
import com.example.u5w1d2.repositories.SubscriptionRepository;
import com.example.u5w1d2.repositories.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final TopicRepository topics;
    private final SubscriptionRepository subs;
    private final MessageRepository messages;
    private final NotificationRepository notifications;

    public MessageService(TopicRepository topics, SubscriptionRepository subs,
                          MessageRepository messages, NotificationRepository notifications) {
        this.topics = topics;
        this.subs = subs;
        this.messages = messages;
        this.notifications = notifications;
    }

    /**
     * Pubblica un messaggio. La regola vive qui: chi non e' iscritto non scrive (403).
     * Nella stessa transazione nasce una riga in messages e una in notifications
     * per ogni altro iscritto. Ritorna DTO gia' mappati: i frame partono dopo il commit.
     */
    @Transactional
    public PublishResult publish(String nomeTopic, AppUser autore, String testo) {
        Topic topic = topics.findByName(nomeTopic)
                .orElseThrow(() -> new TopicNotFoundException(nomeTopic));

        if (!subs.existsByUserAndTopic(autore, topic)) {
            throw new NotSubscribedException(nomeTopic);
        }

        Message saved = messages.save(new Message(topic, autore, testo));

        // una notifica per ogni iscritto, tranne l'autore
        List<AppUser> destinatari = subs.findIscrittiDiversiDa(topic, autore);
        notifications.saveAll(destinatari.stream()
                .map(u -> new Notification(u, saved))
                .toList());

        return new PublishResult(
                MessageView.from(saved),
                destinatari.stream().map(AppUser::getUsername).toList(),
                NotificationView.from(saved));
    }

    @Transactional(readOnly = true)
    public Page<MessageView> history(String nomeTopic, Pageable pageable) {
        Topic topic = topics.findByName(nomeTopic)
                .orElseThrow(() -> new TopicNotFoundException(nomeTopic));
        return messages.findByTopicOrderByCreatedAtDesc(topic, pageable).map(MessageView::from);
    }
}
