package by.tyv.transfer;

import by.tyv.transfer.config.SecurityConfiguration;
import by.tyv.transfer.controller.TransferController;
import by.tyv.transfer.mapper.TransferMapperImpl;
import by.tyv.transfer.service.TransferService;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = TransferController.class)
@Import({TransferMapperImpl.class, SecurityConfiguration.class})
@AutoConfigureWebTestClient
public class BaseContractTest {
    @MockitoBean
    TransferService transferService;

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
