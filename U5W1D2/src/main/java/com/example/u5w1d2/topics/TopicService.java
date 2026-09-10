package com.example.u5w1d2.topics;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Subscription;
import com.example.u5w1d2.entities.Topic;
import com.example.u5w1d2.exceptions.AlreadySubscribedException;
import com.example.u5w1d2.exceptions.TopicNotFoundException;
import com.example.u5w1d2.repositories.SubscriptionRepository;
import com.example.u5w1d2.repositories.TopicRepository;
import com.example.u5w1d2.topics.dto.TopicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private static final Logger log = LoggerFactory.getLogger(TopicService.class);

    private final TopicRepository topics;
    private final SubscriptionRepository subs;

    public TopicService(TopicRepository topics, SubscriptionRepository subs) {
        this.topics = topics;
        this.subs = subs;
    }

    /** Elenco topic con conteggio iscritti e flag 'iscritto' calcolato con una sola query sulle iscrizioni dell'utente. */
    @Transactional(readOnly = true)
    public List<TopicView> list(AppUser current) {
        Set<Long> myTopicIds = subs.findByUser(current).stream()
                .map(s -> s.getTopic().getId())
                .collect(Collectors.toSet());
        return topics.findAll().stream()
                .map(t -> new TopicView(
                        t.getName(),
                        t.getTitle(),
                        subs.countByTopic(t),
                        myTopicIds.contains(t.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicView> mySubscriptions(AppUser current) {
        return subs.findByUser(current).stream()
                .map(Subscription::getTopic)
                .map(t -> new TopicView(t.getName(), t.getTitle(), subs.countByTopic(t), true))
                .toList();
    }

    @Transactional
    public void subscribe(String name, AppUser current) {
        Topic topic = topics.findByName(name).orElseThrow(() -> topicInesistente(name));
        if (subs.existsByUserAndTopic(current, topic)) {
            log.warn("Iscrizione rifiutata: '{}' e' gia' iscritto al topic '{}'", current.getUsername(), name);
            throw new AlreadySubscribedException(name);
        }
        try {
            subs.save(new Subscription(current, topic));
            log.info("Iscrizione al topic '{}' per '{}'", name, current.getUsername());
        } catch (DataIntegrityViolationException ex) {
            // due clic nello stesso istante: il vincolo unico (user_id, topic_id) e' la difesa vera
            log.warn("Doppia iscrizione simultanea a '{}' per '{}': respinta dal vincolo unico",
                    name, current.getUsername());
            throw new AlreadySubscribedException(name);
        }
    }

    @Transactional
    public void unsubscribe(String name, AppUser current) {
        Topic topic = topics.findByName(name).orElseThrow(() -> topicInesistente(name));
        subs.findByUserAndTopic(current, topic).ifPresentOrElse(
                sub -> {
                    subs.delete(sub);
                    log.info("Disiscrizione dal topic '{}' per '{}'", name, current.getUsername());
                },
                () -> log.warn("Disiscrizione a vuoto: '{}' non era iscritto al topic '{}'",
                        current.getUsername(), name));
    }

    private TopicNotFoundException topicInesistente(String name) {
        log.warn("Topic '{}' inesistente", name);
        return new TopicNotFoundException(name);
    }
}
