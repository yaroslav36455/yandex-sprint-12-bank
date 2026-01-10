package by.tyv.exchange.service;

import by.tyv.exchange.model.dto.ExchangeRateRequestDto;
import by.tyv.exchange.model.dto.ExchangeRateResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExchangeService {
    Flux<ExchangeRateResponseDto> getRates();
    Mono<Void> update(List<ExchangeRateRequestDto> exchangeRates);
}
