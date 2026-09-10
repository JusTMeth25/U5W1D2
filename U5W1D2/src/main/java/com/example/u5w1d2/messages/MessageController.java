package com.example.u5w1d2.messages;

import com.example.u5w1d2.auth.CurrentUser;
import com.example.u5w1d2.messages.dto.CreateMessageRequest;
import com.example.u5w1d2.messages.dto.MessageView;
import com.example.u5w1d2.messages.dto.NotificaDestinata;
import com.example.u5w1d2.messages.dto.PublishResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics/{name}/messages")
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService service;
    private final CurrentUser currentUser;
    private final SimpMessagingTemplate messaging;

    public MessageController(MessageService service, CurrentUser currentUser, SimpMessagingTemplate messaging) {
        this.service = service;
        this.currentUser = currentUser;
        this.messaging = messaging;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageView publish(@PathVariable String name, @Valid @RequestBody CreateMessageRequest body) {
        // publish e' transazionale: quando ritorna, il commit e' avvenuto. Solo ora partono i frame.
        PublishResult esito = service.publish(name, currentUser.require(), body.text());

        // Il messaggio e' gia' su database: un guasto del broker non deve far fallire la POST,
        // ma va segnalato come ERROR perche' il realtime e' rotto mentre l'API risponde 201.
        try {
            // 1. a chi ha questa bacheca aperta
            messaging.convertAndSend("/topic/feed/" + name, esito.messaggio());

            // 2. a ogni iscritto, uno per uno; se nessuna sessione corrisponde, il frame viene scartato in silenzio
            for (NotificaDestinata destinata : esito.notifiche()) {
                messaging.convertAndSendToUser(destinata.username(), "/queue/notifications", destinata.notifica());
            }
            log.info("Frame inviati per il messaggio {}: 1 sul feed di '{}', {} personali",
                    esito.messaggio().id(), name, esito.notifiche().size());
        } catch (RuntimeException ex) {
            log.error("Messaggio {} salvato ma invio dei frame WebSocket fallito su '{}': {}",
                    esito.messaggio().id(), name, ex.getMessage(), ex);
        }

        return esito.messaggio();
    }

    @GetMapping
    public Page<MessageView> history(@PathVariable String name,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        currentUser.require();
        return service.history(name, PageRequest.of(page, size));
    }
}
