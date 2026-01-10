package by.tyv.exchangegenerator.service;

import by.tyv.exchangegenerator.enums.CurrencyCode;
import by.tyv.exchangegenerator.service.impl.ExchangeRateGeneratorServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

public class ExchangeRateGeneratorServiceTest {

    @Test
    @DisplayName("Генератор курсов валют")
    public void testExchangeRateGenerator() {
        ExchangeRateGeneratorService exchangeRateGeneratorService = new ExchangeRateGeneratorServiceImpl();
        StepVerifier.create(exchangeRateGeneratorService.generate())
                .assertNext(exchangeRates -> {
                    Assertions.assertThat(exchangeRates).size().isEqualTo(5);
                    exchangeRates.forEach(exchangeRate -> {
                        if (exchangeRate.getCode() == CurrencyCode.RUB) {
                            Assertions.assertThat(exchangeRate.getRate()).isEqualTo(BigDecimal.ONE);
                        } else {
                            Assertions.assertThat(exchangeRate.getRate()).isBetween(BigDecimal.ONE, BigDecimal.valueOf(100));
                        }
                    });
                })
                .verifyComplete();
    }
}
