package com.example.u5w1d2.topics;

import com.example.u5w1d2.auth.CurrentUser;
import com.example.u5w1d2.topics.dto.TopicView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService service;
    private final CurrentUser currentUser;

    public TopicController(TopicService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/topics")
    public List<TopicView> list() {
        return service.list(currentUser.require());
    }

    @PostMapping("/topics/{name}/subscription")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@PathVariable String name) {
        service.subscribe(name, currentUser.require());
    }

    @DeleteMapping("/topics/{name}/subscription")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable String name) {
        service.unsubscribe(name, currentUser.require());
    }

    @GetMapping("/me/subscriptions")
    public List<TopicView> mySubscriptions() {
        return service.mySubscriptions(currentUser.require());
    }
}
