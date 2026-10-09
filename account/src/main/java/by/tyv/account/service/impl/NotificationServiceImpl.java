package by.tyv.account.service.impl;

import by.tyv.account.enums.MessageStatus;
import by.tyv.account.model.dto.NotificationDto;
import by.tyv.account.model.entity.DeferredNotificationEntity;
import by.tyv.account.repository.DeferredNotificationRepository;
import by.tyv.account.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final DeferredNotificationRepository repository;
    private final TransactionalOperator transactionalOperator;
    private final KafkaTemplate<String, NotificationDto> notificationTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mono<Void> saveNotificationNewTransaction(String login, String message) {
        return saveNotification(login, message);
    }

    @Override
    public Mono<Void> saveNotification(String login, String message) {
        DeferredNotificationEntity newNotification = new DeferredNotificationEntity();
        newNotification.setLogin(login);
        newNotification.setMessage(message);
        newNotification.setStatus(MessageStatus.CREATED.toString());

        return Mono.defer(() -> repository.save(newNotification)
                .then());
    }

    @Override
    public Mono<Void> sendCreatedNotifications() {
        return Mono.defer(() -> repository.findAllByStatus(MessageStatus.CREATED.toString())
                .flatMap(entity -> this.sendNotification(entity.getLogin(), entity.getMessage())
                        .then(Mono.fromCallable(() -> {
                            entity.setStatus(MessageStatus.SENT.toString());
                            return entity;
                        }))
                        .flatMap(repository::save)
                        .as(transactionalOperator::transactional)
                        .onErrorResume(throwable -> Mono.empty())
                        .then())
                .then());
    }

    private Mono<Void> sendNotification(String login, String message) {
        NotificationDto notificationDto = new  NotificationDto(login, message);
        UUID key = UUID.nameUUIDFromBytes(notificationDto.toString().getBytes(StandardCharsets.UTF_8));
        return Mono.fromFuture(notificationTemplate.send(NOTIFICATION_TOPIC, key.toString(), notificationDto))
                .doOnSuccess(result -> {
                    var meta = result.getRecordMetadata();
                    log.info("Успешно отправлено сообщение: topic={}, partition={}, offset={}, key={}",
                            meta.topic(), meta.partition(), meta.offset(), key);
                })
                .doOnError(ex -> {
                    log.error("Ошибка отправки сообщения: topic={}, key={}, login={}",
                            NOTIFICATION_TOPIC, key, login, ex);
                })
                .then();
    }
}
