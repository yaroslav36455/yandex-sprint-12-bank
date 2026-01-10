package by.tyv.exchange.repository;

import by.tyv.exchange.model.entity.ExchangeRateEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends ReactiveCrudRepository<ExchangeRateEntity, Long> {
}
