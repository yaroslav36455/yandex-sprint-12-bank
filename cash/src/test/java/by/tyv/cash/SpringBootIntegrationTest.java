package by.tyv.cash;

import by.tyv.cash.config.TestcontainersConfiguration;
import by.tyv.cash.repository.DeferredNotificationRepository;
import by.tyv.cash.scheduler.NotificationScheduler;
import by.tyv.cash.service.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public class SpringBootIntegrationTest {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected WebTestClient webClient;
    @Autowired
    protected DeferredNotificationRepository deferredNotificationRepository;
    @Autowired
    protected NotificationScheduler notificationScheduler;
    @MockitoSpyBean
    protected TokenProvider tokenProvider;

    protected static WireMockServer wireMockServerBlocker = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );
    protected static WireMockServer wireMockServerAccount = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );
    protected static WireMockServer wireMockServerNotification = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );

    static {
        wireMockServerBlocker.start();
        wireMockServerAccount.start();
        wireMockServerNotification.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            wireMockServerBlocker.stop();
            wireMockServerAccount.stop();
            wireMockServerNotification.stop();
        }));
    }

    @BeforeEach
    public void setup() {
        Mockito.doReturn(Mono.just("dummy-token"))
                .when(tokenProvider)
                .getNewTechnical();
    }

    @AfterEach
    public void cleanUp() {
        wireMockServerBlocker.resetAll();
        wireMockServerAccount.resetAll();
        wireMockServerNotification.resetAll();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("clients.blocker-service.url", () -> wireMockServerBlocker.baseUrl());
        registry.add("clients.account-service.url", () -> wireMockServerAccount.baseUrl());
        registry.add("clients.notification-service.url", () -> wireMockServerNotification.baseUrl());
    }
}
