package by.tyv.transfer.service;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> saveNotification(String login, String message);
    Mono<Void> sendCreatedNotifications();
}
