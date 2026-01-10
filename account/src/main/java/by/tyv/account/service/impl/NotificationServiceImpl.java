package by.tyv.account.service.impl;

import by.tyv.account.enums.MessageStatus;
import by.tyv.account.exception.NotificationException;
import by.tyv.account.model.entity.DeferredNotificationEntity;
import by.tyv.account.repository.DeferredNotificationRepository;
import by.tyv.account.service.NotificationService;
import by.tyv.account.service.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final WebClient webClient;
    private final DeferredNotificationRepository repository;
    private final TransactionalOperator transactionalOperator;
    private final TokenProvider tokenProvider;

    public NotificationServiceImpl(@Value("${clients.notification-service.url}") String notificationServiceUrl,
                                   WebClient.Builder webClientBuilder,
                                   DeferredNotificationRepository repository,
                                   TransactionalOperator transactionalOperator,
                                   TokenProvider notificationTokenProvider) {
        this.webClient = webClientBuilder.baseUrl(notificationServiceUrl).build();
        this.repository = repository;
        this.transactionalOperator = transactionalOperator;
        this.tokenProvider = notificationTokenProvider;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
        return tokenProvider.getNewTechnical().flatMap(
                token -> webClient.post()
                        .uri(uriBuilder -> uriBuilder.pathSegment("notifications", login, "message").build())
                        .contentType(MediaType.TEXT_PLAIN)
                        .headers(headers -> headers.setBearerAuth(token))
                        .bodyValue(message)
                        .retrieve()
                        .onStatus(status -> !Objects.equals(status, HttpStatus.OK),
                                resp -> Mono.error(new NotificationException("Неизвестная ошибка")))
                        .toBodilessEntity()
                        .then()
                        .doOnError(throwable -> log.error("Ошибка отправки нотификации", throwable))
        );
    }
}
