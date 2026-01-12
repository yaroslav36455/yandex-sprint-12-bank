package by.tyv.account.service;

import reactor.core.publisher.Mono;

public interface NotificationService {
    String NOTIFICATION_TOPIC = "notification_topic";

    Mono<Void> saveNotification(String login, String message);
    Mono<Void> sendCreatedNotifications();
}
