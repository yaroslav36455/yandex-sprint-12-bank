package by.tyv.transfer.service.impl;

import by.tyv.transfer.enums.CurrencyCode;
import by.tyv.transfer.exception.ExchangeException;
import by.tyv.transfer.mapper.ExchangeMapper;
import by.tyv.transfer.model.bo.Exchange;
import by.tyv.transfer.model.bo.Transfer;
import by.tyv.transfer.model.dto.ExchangeRateResponseDto;
import by.tyv.transfer.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {
    private final WebClient webClient;
    private final ExchangeMapper exchangeMapper;

    public ExchangeServiceImpl(@Value("${clients.exchange-service.url}") String exchangeServiceUrl,
                               WebClient.Builder webClientBuilder,
                               ExchangeMapper exchangeMapper) {
        this.webClient = webClientBuilder.baseUrl(exchangeServiceUrl).build();
        this.exchangeMapper = exchangeMapper;
    }

    @Override
    public Mono<BigDecimal> convertMoney(Transfer transfer) {
        return webClient.get().uri(uriBuilder -> uriBuilder.path("/api/rates").build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToFlux(ExchangeRateResponseDto.class)
                .map(exchangeMapper::toExchangeBO)
                .collectList()
                .map(exchangeList -> convert(exchangeList, transfer))
                .doOnError(throwable -> log.error("Ошибка чтения курса валют", throwable));
    }

    private BigDecimal convert(List<Exchange> exchangeList, Transfer transfer) {
        Exchange sourceExchange = filter(exchangeList, transfer.getSourceCurrency());
        Exchange targetExchange = filter(exchangeList, transfer.getTargetCurrency());

        return transfer.getSourceAmount()
                .multiply(targetExchange.getRate())
                .divide(sourceExchange.getRate(), 2, RoundingMode.DOWN);
    }

    private Exchange filter(List<Exchange> exchangeList, CurrencyCode currencyCode) {
        return exchangeList.stream()
                .filter(exchange -> Objects.equals(currencyCode, exchange.getCurrency()))
                .findFirst()
                .orElseThrow(() -> new ExchangeException("Не найдено курса для валюты %s".formatted(currencyCode)));
    }
}
