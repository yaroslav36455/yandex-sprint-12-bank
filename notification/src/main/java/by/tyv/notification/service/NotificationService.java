package by.tyv.notification.service;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> handleMessage(String login, String message);
}
