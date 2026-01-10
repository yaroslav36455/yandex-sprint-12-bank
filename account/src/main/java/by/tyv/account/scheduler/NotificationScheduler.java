package by.tyv.account.scheduler;

import by.tyv.account.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationService notificationService;

    @Scheduled(fixedDelayString = "${application.scheduling.notification-delay}")
    public void sendNotifications() {
        notificationService.sendCreatedNotifications().block();
    }
}
