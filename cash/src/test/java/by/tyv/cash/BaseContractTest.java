package by.tyv.cash;

import by.tyv.cash.config.SecurityConfiguration;
import by.tyv.cash.controller.CashController;
import by.tyv.cash.service.CashService;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = CashController.class)
@Import(SecurityConfiguration.class)
@AutoConfigureWebTestClient
public abstract class BaseContractTest {
    @MockitoBean
    CashService cashService;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setupRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                .claim("sub", "some-subject")
                .claim("client_id", "some-client-id")
                .claim("scope", "internal_call"))));
    }
}

