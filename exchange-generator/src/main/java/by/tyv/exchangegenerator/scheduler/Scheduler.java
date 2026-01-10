package by.tyv.exchangegenerator.scheduler;

import by.tyv.exchangegenerator.service.ExchangeRateGeneratorService;
import by.tyv.exchangegenerator.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final ExchangeRateGeneratorService generatorService;
    private final ExchangeService exchangeService;

    @Scheduled(fixedDelay = 3000)
    public void generateExchangeRates() {
        generatorService.generate()
                .flatMap(exchangeService::update)
                .subscribe();
    }
}
