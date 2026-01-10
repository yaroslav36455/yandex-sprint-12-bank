package by.tyv.exchange.service.impl;

import by.tyv.exchange.mapper.ExchangeRateMapper;
import by.tyv.exchange.model.dto.ExchangeRateRequestDto;
import by.tyv.exchange.model.dto.ExchangeRateResponseDto;
import by.tyv.exchange.repository.ExchangeRateRepository;
import by.tyv.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper mapper;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Flux<ExchangeRateResponseDto> getRates() {
        return exchangeRateRepository.findAll()
                .map(mapper::toResponseDto);
    }

    @Override
    public Mono<Void> update(List<ExchangeRateRequestDto> exchangeRates) {
        return Mono.defer(() -> exchangeRateRepository.deleteAll()
                .thenMany(exchangeRateRepository.saveAll(mapper.toEntity(exchangeRates)))
                .then())
                .as(transactionalOperator::transactional);
    }
}
