package by.tyv.account.integration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class AccountConsumerStubTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .stubsMode(StubRunnerProperties.StubsMode.LOCAL)
            .downloadStub("by.tyv:notification:1.0.0:stubs");

    private WebClient webClientNotification;

    @BeforeEach
    void setUp() {
        String notifBase = stubRunner.findStubUrl("by.tyv", "notification").toString();
        assertThat(notifBase).isNotBlank();
        webClientNotification = WebClient.builder().baseUrl(notifBase).build();
    }

    @Test
    @DisplayName("POST /notifications/{login}/message, ответ 200")
    void callClientNotificationSuccessTest() {
        StepVerifier.create(
                        webClientNotification.post().uri("/notifications/username/message")
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue("Операция выполнена успешно")
                                .exchangeToMono(clientResponse -> {
                                    Assertions.assertThat(clientResponse.statusCode()).isEqualTo(HttpStatus.OK);
                                    return clientResponse.releaseBody();
                                })
                )
                .verifyComplete();
    }
}
