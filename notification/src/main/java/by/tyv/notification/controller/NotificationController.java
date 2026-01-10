package by.tyv.notification.controller;

import by.tyv.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping(value = "/notifications/{login}/message", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Mono<Void> handleMessage(@PathVariable("login") String login, @RequestBody String message) {
        return notificationService.handleMessage(login, message);
    }
}
