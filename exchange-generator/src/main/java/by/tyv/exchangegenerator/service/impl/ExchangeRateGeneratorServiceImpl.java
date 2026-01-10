package by.tyv.exchangegenerator.service.impl;

import by.tyv.exchangegenerator.enums.CurrencyCode;
import by.tyv.exchangegenerator.model.bo.ExchangeRate;
import by.tyv.exchangegenerator.service.ExchangeRateGeneratorService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class ExchangeRateGeneratorServiceImpl implements ExchangeRateGeneratorService {
    static private final double generateBound = 100.00;
    static private final double updateBoundMin = -0.03;
    static private final double updateBoundMax = 0.03;
    static private final Random random = new Random();
    private List<ExchangeRate> cachedExchangeRates;

    @Override
    public Mono<List<ExchangeRate>> generate() {
        if (Objects.isNull(cachedExchangeRates)) {
            generateNewExchangeRates();
        } else {
            updateExchangeRates();
        }

        return Mono.just(cachedExchangeRates);
    }

    private void generateNewExchangeRates() {
        cachedExchangeRates = Arrays.stream(CurrencyCode.values())
                .map(currencyCode -> currencyCode == CurrencyCode.RUB
                        ? new ExchangeRate(CurrencyCode.RUB, BigDecimal.ONE)
                        : new ExchangeRate(currencyCode, BigDecimal.valueOf(random.nextDouble(1, generateBound))))
                .toList();
    }

    private void updateExchangeRates() {
        cachedExchangeRates = cachedExchangeRates.stream()
                .peek(exchangeRate -> {
                    if (exchangeRate.getCode() != CurrencyCode.RUB) {
                        double rateChange = random.nextDouble(updateBoundMin, updateBoundMax);
                        exchangeRate.setRate(exchangeRate.getRate().add(BigDecimal.valueOf(rateChange)).max(BigDecimal.ONE));
                    }
                }).toList();
    }
}
