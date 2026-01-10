package by.tyv.exchangegenerator.service;

import by.tyv.exchangegenerator.model.bo.ExchangeRate;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExchangeRateGeneratorService {
    Mono<List<ExchangeRate>> generate();
}
