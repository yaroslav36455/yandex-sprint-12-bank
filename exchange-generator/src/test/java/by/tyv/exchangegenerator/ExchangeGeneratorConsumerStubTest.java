package by.tyv.exchangegenerator;

import by.tyv.exchangegenerator.enums.CurrencyCode;
import by.tyv.exchangegenerator.mapper.ExchangeMapperImpl;
import by.tyv.exchangegenerator.model.bo.ExchangeRate;
import by.tyv.exchangegenerator.service.ExchangeService;
import by.tyv.exchangegenerator.service.TokenProvider;
import by.tyv.exchangegenerator.service.impl.ExchangeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;


@SpringJUnitConfig(ExchangeGeneratorConsumerStubTest.Cfg.class)
@AutoConfigureStubRunner(ids = "by.tyv:exchange:1.0.0:stubs", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
public class ExchangeGeneratorConsumerStubTest {

    @Autowired
    ExchangeService exchangeService;

    @TestConfiguration
    static class Cfg {
        @Bean
        ExchangeService exchangeService(StubFinder stubs) {
            String url = stubs.findStubUrl("by.tyv", "exchange").toString();
            return new ExchangeServiceImpl(url, new ExchangeMapperImpl(), WebClient.builder(), getTokenProvider());
        }

        @Bean
        TokenProvider getTokenProvider() {
            return () -> Mono.just("dummy-token");
        }
    }

    @Test
    @DisplayName("Отправка набора курсов валют")
    public void apiUpdate() {
        List<ExchangeRate> exchangeRates = List.of(
                new ExchangeRate(CurrencyCode.BYN, new BigDecimal("55.55")),
                new ExchangeRate(CurrencyCode.RUB, new BigDecimal("1.00")),
                new ExchangeRate(CurrencyCode.IRR, new BigDecimal("98.21")),
                new ExchangeRate(CurrencyCode.CNY, new BigDecimal("1934.67")),
                new ExchangeRate(CurrencyCode.INR, new BigDecimal("167.12"))
        );

        StepVerifier.create(exchangeService.update(exchangeRates))
                .verifyComplete();
    }
}
