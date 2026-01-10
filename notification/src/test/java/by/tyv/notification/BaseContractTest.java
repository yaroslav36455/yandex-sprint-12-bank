package by.tyv.notification;

import by.tyv.notification.config.SecurityConfiguration;
import by.tyv.notification.controller.NotificationController;
import by.tyv.notification.service.NotificationService;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = NotificationController.class)
@Import(SecurityConfiguration.class)
@AutoConfigureWebTestClient
public class BaseContractTest {
    @MockitoBean
    NotificationService notificationService;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    public void beforeEach() {
        Mockito.doReturn(Mono.empty())
                .when(notificationService)
                .handleMessage(Mockito.anyString(), Mockito.anyString());

        RestAssuredWebTestClient.webTestClient(webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                .claim("sub", "some-subject")
                .claim("client_id", "some-client-id")
                .claim("scope", "internal_call"))));
    }
}
