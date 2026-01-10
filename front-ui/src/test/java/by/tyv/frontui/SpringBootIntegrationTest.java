package by.tyv.frontui;

import by.tyv.frontui.service.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class SpringBootIntegrationTest {
    protected static WireMockServer wireMockServerAccount = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );
    protected static WireMockServer wireMockServerTransfer = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );
    protected static WireMockServer wireMockServerCash = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );

    static {
        wireMockServerAccount.start();
        wireMockServerTransfer.start();
        wireMockServerCash.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            wireMockServerAccount.stop();
            wireMockServerTransfer.stop();
            wireMockServerCash.stop();
        }));
    }

    @MockitoSpyBean
    protected TokenProvider tokenProvider;

    @BeforeEach
    public void setup() {
        Mockito.doReturn(Mono.just("dummy-token"))
                .when(tokenProvider)
                .getNewTechnical();
    }

    @AfterEach
    public void tearDown() {
        wireMockServerAccount.resetAll();
        wireMockServerTransfer.resetAll();
        wireMockServerCash.resetAll();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("clients.account-service.url", () -> wireMockServerAccount.baseUrl());
        registry.add("clients.transfer-service.url", () -> wireMockServerTransfer.baseUrl());
        registry.add("clients.cash-service.url", () -> wireMockServerCash.baseUrl());
    }

    @Autowired
    protected WebTestClient webClient;
    @Autowired
    protected ObjectMapper objectMapper;
}
