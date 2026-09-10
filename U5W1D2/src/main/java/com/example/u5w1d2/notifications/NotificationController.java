package com.example.u5w1d2.notifications;

import com.example.u5w1d2.auth.CurrentUser;
import com.example.u5w1d2.notifications.dto.NotificationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public Page<NotificationItem> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        // La query parte dal token, non da un parametro: notifiche di altri utenti irraggiungibili.
        return service.list(currentUser.require(), PageRequest.of(page, size));
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", service.unreadCount(currentUser.require()));
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll() {
        service.readAll(currentUser.require());
    }

    /** Segna letta la sola notifica aperta: e' quello che fa scendere il badge di uno. */
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@PathVariable Long id) {
        service.read(id, currentUser.require());
    }
}
