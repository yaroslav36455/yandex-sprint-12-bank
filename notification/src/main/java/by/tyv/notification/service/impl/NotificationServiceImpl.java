package by.tyv.notification.service.impl;

import by.tyv.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void handleMessage(String login, String message) {
        log.info("Пользователь '{}' получил сообщение: {}", login, message);
    }
}
