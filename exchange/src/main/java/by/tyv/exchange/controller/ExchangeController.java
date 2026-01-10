package by.tyv.exchange.controller;

import by.tyv.exchange.model.dto.ExchangeRateRequestDto;
import by.tyv.exchange.model.dto.ExchangeRateResponseDto;
import by.tyv.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ExchangeController {
    private final ExchangeService exchangeService;

    /*
    и) GET "/api/rates" - эндпоинт получения курсов валют
    Возвращает JSON со списком курсов валют:
                title - название валюты
                name - код валюты
                value - курс валюты по отношению к рублю (для рубля 1)
    */
    @GetMapping("/api/rates")
    public Flux<ExchangeRateResponseDto> getApiRates() {
        return exchangeService.getRates();
    }

    @PostMapping("/api/update")
    public Mono<Void> postApiUpdate(@RequestBody List<ExchangeRateRequestDto> exchangeRates) {
        return exchangeService.update(exchangeRates);
    }
}
