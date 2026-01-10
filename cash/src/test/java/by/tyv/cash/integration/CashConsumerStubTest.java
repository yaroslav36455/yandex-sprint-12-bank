package by.tyv.cash.integration;

import by.tyv.cash.enums.Action;
import by.tyv.cash.enums.CurrencyCode;
import by.tyv.cash.model.dto.BlockerResponseDto;
import by.tyv.cash.model.dto.OperationCashRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CashConsumerStubTest{

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .stubsMode(StubRunnerProperties.StubsMode.LOCAL)
            .downloadStub("by.tyv:blocker:1.0.0:stubs")
            .downloadStub("by.tyv:account:1.0.0:stubs")
            .downloadStub("by.tyv:notification:1.0.0:stubs");

    private WebClient webClientBlocker;
    private WebClient webClientAccount;
    private WebClient webClientNotification;

    @BeforeEach
    void setUp() {
        String blockerBase = stubRunner.findStubUrl("by.tyv", "blocker").toString();
        String accountBase = stubRunner.findStubUrl("by.tyv", "account").toString();
        String notifBase   = stubRunner.findStubUrl("by.tyv", "notification").toString();

        assertThat(blockerBase).isNotBlank();
        assertThat(accountBase).isNotBlank();
        assertThat(notifBase).isNotBlank();

        webClientBlocker = WebClient.builder().baseUrl(blockerBase).build();
        webClientAccount = WebClient.builder().baseUrl(accountBase).build();
        webClientNotification = WebClient.builder().baseUrl(notifBase).build();
    }

    @ParameterizedTest
    @MethodSource("currencyProvider")
    @DisplayName("POST /operations/available/cash, ответ 200")
    void callClientBlockerSuccessTest(CurrencyCode currencyCode, Action action, BigDecimal amount) {
        OperationCashRequestDto operationCashRequestDto = new OperationCashRequestDto()
                .setCurrency(currencyCode)
                .setAction(action)
                .setAmount(amount);

        StepVerifier.create(
                        webClientBlocker.post().uri("/operations/available/cash")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(operationCashRequestDto)
                                .retrieve()
                                .bodyToMono(BlockerResponseDto.class)
                )
                .expectNext(new BlockerResponseDto(true))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("currencyProvider")
    @DisplayName("POST /account/{login}/operation/cash, ответ 200")
    void callClientAccountSuccessTest(CurrencyCode currencyCode, Action action, BigDecimal amount) {
        OperationCashRequestDto operationCashRequestDto = new OperationCashRequestDto()
                .setCurrency(currencyCode)
                .setAction(action)
                .setAmount(amount);

        StepVerifier.create(
                        webClientAccount.post().uri("/account/username/operation/cash")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(operationCashRequestDto)
                                .exchangeToMono(clientResponse -> {
                                    Assertions.assertThat(clientResponse.statusCode()).isEqualTo(HttpStatus.OK);
                                    return clientResponse.releaseBody();
                                })
                )
                .verifyComplete();
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

    private static Stream<Arguments> currencyProvider() {
        List<Arguments> arguments = new ArrayList<>();
        Random random = new Random();
        for (var cc : CurrencyCode.values()) {
            for (var a : Action.values()) {
                BigDecimal amount = BigDecimal.valueOf(random.nextDouble(0, 100.00)).setScale(2, RoundingMode.HALF_UP);
                arguments.add(Arguments.of(cc, a, amount));
            }
        }
        return arguments.stream();
    }
}
