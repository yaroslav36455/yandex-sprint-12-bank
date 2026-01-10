package by.tyv.notification.service.impl;

import by.tyv.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public Mono<Void> handleMessage(String login, String message) {
        return Mono.fromRunnable(() -> log.info("Пользователь '{}' получил сообщение: {}", login, message));
    }
}
