package com.example.u5w1d2.messages;

import com.example.u5w1d2.auth.CurrentUser;
import com.example.u5w1d2.messages.dto.CreateMessageRequest;
import com.example.u5w1d2.messages.dto.MessageView;
import com.example.u5w1d2.messages.dto.PublishResult;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics/{name}/messages")
public class MessageController {

    private final MessageService service;
    private final CurrentUser currentUser;

    public MessageController(MessageService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageView publish(@PathVariable String name, @Valid @RequestBody CreateMessageRequest body) {
        PublishResult esito = service.publish(name, currentUser.require(), body.text());
        // Passo 5: qui partono i frame WebSocket sulle due destinazioni, dopo il commit.
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
