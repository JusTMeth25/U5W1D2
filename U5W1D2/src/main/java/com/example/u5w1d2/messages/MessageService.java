package com.example.u5w1d2.messages;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Message;
import com.example.u5w1d2.entities.Notification;
import com.example.u5w1d2.entities.Topic;
import com.example.u5w1d2.exceptions.NotSubscribedException;
import com.example.u5w1d2.exceptions.TopicNotFoundException;
import com.example.u5w1d2.messages.dto.MessageView;
import com.example.u5w1d2.messages.dto.NotificaDestinata;
import com.example.u5w1d2.messages.dto.NotificationView;
import com.example.u5w1d2.messages.dto.PublishResult;
import com.example.u5w1d2.repositories.MessageRepository;
import com.example.u5w1d2.repositories.NotificationRepository;
import com.example.u5w1d2.repositories.SubscriptionRepository;
import com.example.u5w1d2.repositories.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

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
                .orElseThrow(() -> topicInesistente(nomeTopic));

        if (!subs.existsByUserAndTopic(autore, topic)) {
            log.warn("Pubblicazione rifiutata: '{}' non e' iscritto al topic '{}'",
                    autore.getUsername(), nomeTopic);
            throw new NotSubscribedException(nomeTopic);
        }

        Message saved = messages.save(new Message(topic, autore, testo));

        // una notifica per ogni iscritto, tranne l'autore
        List<AppUser> destinatari = subs.findIscrittiDiversiDa(topic, autore);
        List<Notification> create = notifications.saveAll(destinatari.stream()
                .map(u -> new Notification(u, saved))
                .toList());

        log.info("Messaggio {} pubblicato su '{}' da '{}': {} notifiche create",
                saved.getId(), nomeTopic, autore.getUsername(), create.size());

        // saveAll ha assegnato gli id: ogni destinatario riceve il proprio, e potra'
        // segnare letta quella notifica senza dover ricaricare l'elenco.
        return new PublishResult(
                MessageView.from(saved),
                create.stream()
                        .map(n -> new NotificaDestinata(n.getRecipient().getUsername(), NotificationView.from(n)))
                        .toList());
    }

    @Transactional(readOnly = true)
    public Page<MessageView> history(String nomeTopic, Pageable pageable) {
        Topic topic = topics.findByName(nomeTopic)
                .orElseThrow(() -> topicInesistente(nomeTopic));
        Page<MessageView> pagina = messages.findByTopicOrderByCreatedAtDesc(topic, pageable)
                .map(MessageView::from);
        log.debug("Storico di '{}': pagina {} con {} messaggi su {} totali",
                nomeTopic, pageable.getPageNumber(), pagina.getNumberOfElements(), pagina.getTotalElements());
        return pagina;
    }

    private TopicNotFoundException topicInesistente(String nomeTopic) {
        log.warn("Topic '{}' inesistente", nomeTopic);
        return new TopicNotFoundException(nomeTopic);
    }
}
