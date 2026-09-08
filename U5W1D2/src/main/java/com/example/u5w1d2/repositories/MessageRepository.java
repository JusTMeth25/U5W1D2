package com.example.u5w1d2.repositories;

import com.example.u5w1d2.entities.Message;
import com.example.u5w1d2.entities.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    /** Storico paginato dal piu' recente. Usa l'indice (topic_id, created_at). */
    Page<Message> findByTopicOrderByCreatedAtDesc(Topic topic, Pageable pageable);
}
