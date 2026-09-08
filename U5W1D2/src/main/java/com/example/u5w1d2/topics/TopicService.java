package com.example.u5w1d2.topics;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Subscription;
import com.example.u5w1d2.entities.Topic;
import com.example.u5w1d2.exceptions.AlreadySubscribedException;
import com.example.u5w1d2.exceptions.TopicNotFoundException;
import com.example.u5w1d2.repositories.SubscriptionRepository;
import com.example.u5w1d2.repositories.TopicRepository;
import com.example.u5w1d2.topics.dto.TopicView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicService {

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
        Topic topic = topics.findByName(name).orElseThrow(() -> new TopicNotFoundException(name));
        if (subs.existsByUserAndTopic(current, topic)) {
            throw new AlreadySubscribedException(name);
        }
        try {
            subs.save(new Subscription(current, topic));
        } catch (DataIntegrityViolationException ex) {
            // due clic nello stesso istante: il vincolo unico (user_id, topic_id) e' la difesa vera
            throw new AlreadySubscribedException(name);
        }
    }

    @Transactional
    public void unsubscribe(String name, AppUser current) {
        Topic topic = topics.findByName(name).orElseThrow(() -> new TopicNotFoundException(name));
        subs.findByUserAndTopic(current, topic).ifPresent(subs::delete);
    }
}
