package by.tyv.cash.repository;

import by.tyv.cash.model.entity.DeferredNotificationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DeferredNotificationRepository extends ReactiveCrudRepository<DeferredNotificationEntity, Long> {
    Flux<DeferredNotificationEntity> findAllByStatus(String status);
}
