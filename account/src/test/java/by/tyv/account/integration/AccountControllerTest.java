package by.tyv.account.integration;

import by.tyv.account.SpringBootIntegrationTest;
import by.tyv.account.enums.CashAction;
import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.enums.MessageStatus;
import by.tyv.account.model.dto.OperationCashRequestDto;
import by.tyv.account.model.dto.TransferRequestDto;
import by.tyv.account.model.entity.DeferredNotificationEntity;
import by.tyv.account.util.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

public class AccountControllerTest extends SpringBootIntegrationTest {

    @Test
    @DisplayName("POST /account/{login}/operation/transfer, успешный перевод денег")
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    public void transferOperationSuccessTest() {
        TransferRequestDto transferRequestDto = new TransferRequestDto()
                .setTargetLogin("TargetLogin")
                .setSourceAmount(new BigDecimal("50.20"))
                .setTargetAmount(new BigDecimal("25.10"))
                .setSourceCurrency(CurrencyCode.BYN)
                .setTargetCurrency(CurrencyCode.IRR);
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/account/{login}/operation/transfer")
                .buildAndExpand("SourceLogin").toUriString())
                .body(Mono.just(transferRequestDto), TransferRequestDto.class)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(accountRepository.findById(1L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(new BigDecimal("1949.80"));
                })
                .verifyComplete();
        StepVerifier.create(accountRepository.findById(2L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(new BigDecimal("2025.10"));
                })
                .verifyComplete();

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(notifications -> {
                    Assertions.assertThat(notifications)
                            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt")
                            .containsExactlyInAnyOrder(
                                    new DeferredNotificationEntity()
                                            .setStatus(MessageStatus.CREATED.toString())
                                            .setLogin("SourceLogin")
                                            .setMessage("Вы отправили на счёт TargetLogin сумму 50.20 BYN"),
                                    new DeferredNotificationEntity()
                                            .setStatus(MessageStatus.CREATED.toString())
                                            .setLogin("TargetLogin")
                                            .setMessage("Вы получили на счёт от SourceLogin сумму 25.10 IRR")
                            );

                    Assertions.assertThat(notifications)
                            .allSatisfy(n -> Assertions.assertThat(n.getCreatedAt())
                                    .isNotNull()
                                    .isBeforeOrEqualTo(LocalDateTime.now())
                            )
                            .extracting(DeferredNotificationEntity::getId)
                            .isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /account/{login}/operation/transfer, попытка перевода денег, недостаточно денег на счету")
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    public void transferOperationMoreMoneyThanAvailableTest() {
        TransferRequestDto transferRequestDto = new TransferRequestDto()
                .setTargetLogin("TargetLogin")
                .setSourceAmount(new BigDecimal("2100"))
                .setTargetAmount(new BigDecimal("1050"))
                .setSourceCurrency(CurrencyCode.BYN)
                .setTargetCurrency(CurrencyCode.IRR);
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/account/{login}/operation/transfer")
                        .buildAndExpand("SourceLogin").toUriString())
                .body(Mono.just(transferRequestDto), TransferRequestDto.class)
                .exchange()
                .expectStatus().isBadRequest();

        StepVerifier.create(accountRepository.findById(1L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(new BigDecimal("2000.00"));
                })
                .verifyComplete();
        StepVerifier.create(accountRepository.findById(2L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(new BigDecimal("2000.00"));
                })
                .verifyComplete();

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(notification -> {
                    Assertions.assertThat(notification)
                            .singleElement()
                            .usingRecursiveComparison()
                            .ignoringFields("id", "createdAt")
                            .isEqualTo(
                                    new DeferredNotificationEntity()
                                            .setStatus(MessageStatus.CREATED.toString())
                                            .setLogin("SourceLogin")
                                            .setMessage("Недостаточно денег на счету")
                            );

                    Assertions.assertThat(notification)
                            .singleElement()
                            .satisfies(t -> Assertions.assertThat(t.getCreatedAt())
                                    .isNotNull()
                                    .isBeforeOrEqualTo(LocalDateTime.now())
                            )
                            .extracting(DeferredNotificationEntity::getId)
                            .isNotNull();
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cashOperationSource")
    @DisplayName("POST /account/{login}/operation/cash, успешные операции с кешем")
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    public void cashOperationSuccessTest(CashAction cashAction, BigDecimal resultAmount, String message) {
        OperationCashRequestDto operationCashRequestDto = new OperationCashRequestDto()
                .setAction(cashAction)
                .setCurrency(CurrencyCode.CNY)
                .setAmount(new BigDecimal("100.00"));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/account/{login}/operation/cash")
                        .buildAndExpand("SourceLogin").toUriString())
                .body(Mono.just(operationCashRequestDto), OperationCashRequestDto.class)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(accountRepository.findById(3L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(resultAmount);
                })
                .verifyComplete();

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(transfers -> {
                    Assertions.assertThat(transfers)
                            .singleElement()
                            .usingRecursiveComparison()
                            .ignoringFields("createdAt")
                            .isEqualTo(new DeferredNotificationEntity()
                                    .setId(1L)
                                    .setStatus(MessageStatus.CREATED.toString())
                                    .setLogin("SourceLogin")
                                    .setMessage(message)
                            );

                    Assertions.assertThat(transfers)
                            .allSatisfy(t -> Assertions.assertThat(t.getCreatedAt())
                                    .isNotNull()
                                    .isBeforeOrEqualTo(LocalDateTime.now())
                            );
                })
                .verifyComplete();
    }

    private static Stream<Arguments> cashOperationSource() {
        return Stream.of(
                Arguments.of(CashAction.GET, new BigDecimal("1900.00"), "Вы сняли деньги со счёта в размере 100.00 CNY"),
                Arguments.of(CashAction.PUT, new BigDecimal("2100.00"), "Вы положили деньги на счёт в размере 100.00 CNY")
        );
    }


    @Test
    @DisplayName("POST /account/{login}/operation/cash, попытка снять со счёта больше денег, чем доступно")
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    public void cashOperationWithdrawMoreMoneyThanAvailableTest() {
        OperationCashRequestDto operationCashRequestDto = new OperationCashRequestDto()
                .setAction(CashAction.GET)
                .setCurrency(CurrencyCode.CNY)
                .setAmount(new BigDecimal("2100.00"));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/account/{login}/operation/cash")
                        .buildAndExpand("SourceLogin").toUriString())
                .body(Mono.just(operationCashRequestDto), OperationCashRequestDto.class)
                .exchange()
                .expectStatus().isBadRequest();

        StepVerifier.create(accountRepository.findById(3L))
                .assertNext(account -> {
                    Assertions.assertThat(account).isNotNull();
                    Assertions.assertThat(account.getBalance())
                            .usingComparator(BigDecimal::compareTo)
                            .isEqualTo(new BigDecimal("2000.00"));
                })
                .verifyComplete();

        StepVerifier.create(deferredNotificationRepository.findAll().collectList())
                .assertNext(transfers -> {
                    Assertions.assertThat(transfers)
                            .singleElement()
                            .usingRecursiveComparison()
                            .ignoringFields("createdAt")
                            .isEqualTo(new DeferredNotificationEntity()
                                    .setId(1L)
                                    .setStatus(MessageStatus.CREATED.toString())
                                    .setLogin("SourceLogin")
                                    .setMessage("Недостаточно денег на счету")
                            );

                    Assertions.assertThat(transfers)
                            .allSatisfy(t -> Assertions.assertThat(t.getCreatedAt())
                                    .isNotNull()
                                    .isBeforeOrEqualTo(LocalDateTime.now())
                            );
                })
                .verifyComplete();
    }


    @Test
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    @DisplayName("GET /account/{login}, успешное чтение всех счетов пользователя")
    public void getAccounts() throws Exception {
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .get().uri(fromPath("/account/{login}").buildAndExpand("TargetLogin").toUriString())
                .header(HttpStatus.ACCEPTED.name(), MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(TestUtils.readResource("json/accounts_response.json"));
    }
}
