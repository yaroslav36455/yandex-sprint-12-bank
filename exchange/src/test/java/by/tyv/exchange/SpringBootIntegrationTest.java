package by.tyv.exchange;

import by.tyv.exchange.config.TestcontainersConfiguration;
import by.tyv.exchange.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public class SpringBootIntegrationTest {
    @Autowired
    protected WebTestClient webClient;
    @Autowired
    protected ExchangeRateRepository  exchangeRateRepository;
}
