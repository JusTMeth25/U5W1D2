package com.example.u5w1d2.repositories;

import com.example.u5w1d2.entities.AppUser;
import com.example.u5w1d2.entities.Subscription;
import com.example.u5w1d2.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByUserAndTopic(AppUser user, Topic topic);

    Optional<Subscription> findByUserAndTopic(AppUser user, Topic topic);

    List<Subscription> findByUser(AppUser user);

    /** Conteggio iscritti a un topic: Spring Data deriva la query dal nome. */
    long countByTopic(Topic topic);

    /** Iscritti a un topic diversi dall'autore: destinatari delle notifiche (Passo 4). */
    @Query("SELECT s.user FROM Subscription s WHERE s.topic = :topic AND s.user <> :author")
    List<AppUser> findIscrittiDiversiDa(@Param("topic") Topic topic, @Param("author") AppUser author);
}
