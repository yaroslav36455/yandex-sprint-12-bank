package by.tyv.transfer.integration;

import by.tyv.transfer.enums.CurrencyCode;
import by.tyv.transfer.model.dto.AccountTransferRequestDto;
import by.tyv.transfer.model.dto.BlockerCheckRequestDto;
import by.tyv.transfer.model.dto.BlockerResponseDto;
import by.tyv.transfer.model.dto.ExchangeRateResponseDto;
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

class TransferConsumerStubTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .stubsMode(StubRunnerProperties.StubsMode.LOCAL)
            .downloadStub("by.tyv:blocker:1.0.0:stubs")
            .downloadStub("by.tyv:account:1.0.0:stubs")
            .downloadStub("by.tyv:notification:1.0.0:stubs")
            .downloadStub("by.tyv:exchange:1.0.0:stubs");

    private WebClient webClientBlocker;
    private WebClient webClientAccount;
    private WebClient webClientNotification;
    private WebClient webClientExchange;

    @BeforeEach
    void setUp() {
        String blockerBase  = stubRunner.findStubUrl("by.tyv", "blocker").toString();
        String accountBase  = stubRunner.findStubUrl("by.tyv", "account").toString();
        String notifBase    = stubRunner.findStubUrl("by.tyv", "notification").toString();
        String exchangeBase = stubRunner.findStubUrl("by.tyv", "exchange").toString();

        assertThat(blockerBase).isNotBlank();
        assertThat(accountBase).isNotBlank();
        assertThat(notifBase).isNotBlank();
        assertThat(exchangeBase).isNotBlank();

        webClientBlocker = WebClient.builder().baseUrl(blockerBase).build();
        webClientAccount = WebClient.builder().baseUrl(accountBase).build();
        webClientNotification = WebClient.builder().baseUrl(notifBase).build();
        webClientExchange = WebClient.builder().baseUrl(exchangeBase).build();
    }

    @ParameterizedTest
    @MethodSource("currencyProvider")
    @DisplayName("POST /operations/available/transfer, ответ 200")
    void callClientBlockerSuccessTest(CurrencyCode sourceCode, CurrencyCode targetCode, BigDecimal amount) {
        BlockerCheckRequestDto operationCashRequestDto = new BlockerCheckRequestDto()
                .setSourceCurrency(sourceCode)
                .setTargetCurrency(targetCode)
                .setSourceAmount(amount);

        StepVerifier.create(
                        webClientBlocker.post().uri("/operations/available/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(operationCashRequestDto)
                                .retrieve()
                                .bodyToMono(BlockerResponseDto.class)
                )
                .expectNext(new BlockerResponseDto(true))
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /account/{login}/operation/transfer, ответ 200")
    void callClientAccountSuccessTest() {
        AccountTransferRequestDto accountTransferRequest = new AccountTransferRequestDto();
        accountTransferRequest.setTargetLogin("targetLogin");
        accountTransferRequest.setTargetAmount(new BigDecimal("10000.00"));
        accountTransferRequest.setSourceAmount(new BigDecimal("5000.00"));
        accountTransferRequest.setSourceCurrency(CurrencyCode.BYN);
        accountTransferRequest.setTargetCurrency(CurrencyCode.IRR);

        StepVerifier.create(
                        webClientAccount.post().uri("/account/sourceLogin/operation/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(accountTransferRequest)
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

    @Test
    @DisplayName("GET /api/rates, ответ 200")
    void callClientExchangeSuccessTest() {
        StepVerifier.create(
                webClientExchange.get().uri("/api/rates")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(ExchangeRateResponseDto.class)
                        .collectList()
                )
                .assertNext(list -> Assertions.assertThat(list)
                        .containsExactlyInAnyOrder(
                                new ExchangeRateResponseDto("Белорусский Рубль", new BigDecimal("123.45"), CurrencyCode.BYN),
                                new ExchangeRateResponseDto("Российский Рубль", new BigDecimal("98.76"), CurrencyCode.RUB)))
                .verifyComplete();
    }

    private static Stream<Arguments> currencyProvider() {
        List<Arguments> arguments = new ArrayList<>();
        Random random = new Random();
        for (var sourceCurrency : CurrencyCode.values()) {
            for (var targetCurrency : CurrencyCode.values()) {
                BigDecimal amount = BigDecimal.valueOf(random.nextDouble(0, 100.00)).setScale(2, RoundingMode.HALF_UP);
                arguments.add(Arguments.of(sourceCurrency, targetCurrency, amount));
            }
        }

        return arguments.stream();
    }
}
