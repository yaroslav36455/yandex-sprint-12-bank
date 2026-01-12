package by.tyv.account;

import by.tyv.account.config.TestcontainersConfiguration;
import by.tyv.account.repository.AccountRepository;
import by.tyv.account.repository.DeferredNotificationRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
public abstract class SpringBootIntegrationTest {
    @Autowired
    protected WebTestClient webClient;
    @Autowired
    protected DeferredNotificationRepository deferredNotificationRepository;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    ReactiveOAuth2AuthorizedClientService authorizedClientService;


    protected static WireMockServer wireMockServerKeycloak = new WireMockServer(
            WireMockConfiguration.options().dynamicPort()
    );

    static {
        wireMockServerKeycloak.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            wireMockServerKeycloak.stop();
        }));
    }

    @BeforeEach
    void beforeEach() {
        authorizedClientService.removeAuthorizedClient("user-manager-cc", "system").block();
    }

    @AfterEach
    public void cleanUp() {
        wireMockServerKeycloak.resetAll();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("clients.keycloak.url", () -> wireMockServerKeycloak.baseUrl());
        registry.add("spring.security.oauth2.client.provider.keycloak.token-uri",
                () -> wireMockServerKeycloak.baseUrl() + "/realms/test-realm/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> wireMockServerKeycloak.baseUrl() + "/.well-known/jwks.json");
    }
}
