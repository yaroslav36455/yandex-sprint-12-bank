package by.tyv.notification.integration;

import by.tyv.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class NotificationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoSpyBean
    private NotificationService notificationService;

    @Test
    @DisplayName("POST /notification/{login}/message ")
    public void handleMessage() {
        webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                 .post().uri("/notifications/username/message")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Операция запрещена")
                .exchange()
                .expectStatus().isOk();
    }
}
