package by.tyv.transfer.integration;

import by.tyv.transfer.SpringBootIntegrationTest;
import by.tyv.transfer.enums.CurrencyCode;
import by.tyv.transfer.enums.MessageStatus;
import by.tyv.transfer.model.dto.*;
import by.tyv.transfer.model.entity.DeferredNotificationEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

public class TransferControllerTest extends SpringBootIntegrationTest {

    @Test
    @DisplayName("POST /transfer/{login} - успешный перевод денег со счёта на счёт")
    @Sql("/sql/clean.sql")
    public void postTransferSuccess() throws JsonProcessingException {
        String sourceLogin = "sourceUsername";
        String targetLogin = "targetUsername";

        BlockerCheckRequestDto operationCheckRequestDto = new BlockerCheckRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        BlockerResponseDto blockerResponseDto = new BlockerResponseDto(true);
        wireMockServerBlocker.stubFor(WireMock.post(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(blockerResponseDto))));

        List<ExchangeRateResponseDto> exchangeRates = List.of(
                new ExchangeRateResponseDto("Иранский Риал", new BigDecimal("2.0"), CurrencyCode.IRR),
                new ExchangeRateResponseDto("Белорусский Рубль", new BigDecimal("0.1"), CurrencyCode.BYN),
                new ExchangeRateResponseDto("Индийская Рупия", new BigDecimal("3.5"), CurrencyCode.INR),
                new ExchangeRateResponseDto("Китайский Юань", new BigDecimal("4.2"), CurrencyCode.CNY),
                new ExchangeRateResponseDto("Российский Рубль", new BigDecimal("1.0"), CurrencyCode.RUB));
        wireMockServerExchange.stubFor(WireMock.get(WireMock.urlPathEqualTo(fromPath("/api/rates").toUriString()))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(exchangeRates))));

        AccountTransferRequestDto accountTransferRequestDto = new AccountTransferRequestDto()
                .setTargetAmount(new BigDecimal("200.00"))
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR)
                .setTargetLogin(targetLogin);
        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        TransferRequestDto transferRequestDto = new TransferRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setTargetLogin(targetLogin)
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post()
                .uri(fromPath("/transfer/{login}").buildAndExpand(sourceLogin).toUriString())
                .body(Mono.just(transferRequestDto), TransferRequestDto.class)
                .exchange()
                .expectStatus().isOk();

        wireMockServerBlocker.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto))));

        wireMockServerExchange.verify(1, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/rates"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE)));

        wireMockServerAccount.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto))));


        DeferredNotificationEntity expectedEntity = new DeferredNotificationEntity()
                .setStatus(MessageStatus.CREATED.toString())
                .setLogin(sourceLogin)
                .setMessage("Операция выполнена успешно");
        StepVerifier.create(deferredNotificationRepository.findAll()
                        .collectList())
                .assertNext(deferredNotificationEntities -> {
                    Assertions.assertThat(deferredNotificationEntities).hasSize(1);
                    DeferredNotificationEntity notificationEntity = deferredNotificationEntities.stream().findFirst().get();
                    Assertions.assertThat(expectedEntity)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "createdAt")
                            .isEqualTo(notificationEntity);
                    Assertions.assertThat(notificationEntity.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /transfer/{login} - блокировщик отклоняет операцию")
    @Sql("/sql/clean.sql")
    public void postTransferBlocked() throws JsonProcessingException {
        String sourceLogin = "sourceUsername";
        String targetLogin = "targetUsername";

        BlockerCheckRequestDto operationCheckRequestDto = new BlockerCheckRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        BlockerResponseDto blockerResponseDto = new BlockerResponseDto(false);
        wireMockServerBlocker.stubFor(WireMock.post(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(blockerResponseDto))));

        List<ExchangeRateResponseDto> exchangeRates = List.of(
                new ExchangeRateResponseDto("Иранский Риал", new BigDecimal("2.0"), CurrencyCode.IRR),
                new ExchangeRateResponseDto("Белорусский Рубль", new BigDecimal("0.1"), CurrencyCode.BYN),
                new ExchangeRateResponseDto("Индийская Рупия", new BigDecimal("3.5"), CurrencyCode.INR),
                new ExchangeRateResponseDto("Китайский Юань", new BigDecimal("4.2"), CurrencyCode.CNY),
                new ExchangeRateResponseDto("Российский Рубль", new BigDecimal("1.0"), CurrencyCode.RUB));
        wireMockServerExchange.stubFor(WireMock.get(WireMock.urlPathEqualTo(fromPath("/api/rates").toUriString()))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(exchangeRates))));

        AccountTransferRequestDto accountTransferRequestDto = new AccountTransferRequestDto()
                .setTargetAmount(new BigDecimal("200.00"))
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR)
                .setTargetLogin(targetLogin);
        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        TransferRequestDto transferRequestDto = new TransferRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setTargetLogin(targetLogin)
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post()
                .uri(fromPath("/transfer/{login}").buildAndExpand(sourceLogin).toUriString())
                .body(Mono.just(transferRequestDto), TransferRequestDto.class)
                .exchange()
                .expectStatus().isOk();

        wireMockServerBlocker.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto))));

        wireMockServerExchange.verify(0, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/rates"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE)));

        wireMockServerAccount.verify(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto))));


        DeferredNotificationEntity expectedEntity = new DeferredNotificationEntity()
                .setStatus(MessageStatus.CREATED.toString())
                .setLogin(sourceLogin)
                .setMessage("Операция запрещена");
        StepVerifier.create(deferredNotificationRepository.findAll()
                        .collectList())
                .assertNext(deferredNotificationEntities -> {
                    Assertions.assertThat(deferredNotificationEntities).hasSize(1);
                    DeferredNotificationEntity notificationEntity = deferredNotificationEntities.stream().findFirst().get();
                    Assertions.assertThat(expectedEntity)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "createdAt")
                            .isEqualTo(notificationEntity);
                    Assertions.assertThat(notificationEntity.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /transfer/{login} - недостаточно денег на счету")
    @Sql("/sql/clean.sql")
    public void postTransferGetNotEnoughMoney() throws JsonProcessingException {
        String sourceLogin = "sourceUsername";
        String targetLogin = "targetUsername";

        BlockerCheckRequestDto operationCheckRequestDto = new BlockerCheckRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        BlockerResponseDto blockerResponseDto = new BlockerResponseDto(true);
        wireMockServerBlocker.stubFor(WireMock.post(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(blockerResponseDto))));

        List<ExchangeRateResponseDto> exchangeRates = List.of(
                new ExchangeRateResponseDto("Иранский Риал", new BigDecimal("2.0"), CurrencyCode.IRR),
                new ExchangeRateResponseDto("Белорусский Рубль", new BigDecimal("0.1"), CurrencyCode.BYN),
                new ExchangeRateResponseDto("Индийская Рупия", new BigDecimal("3.5"), CurrencyCode.INR),
                new ExchangeRateResponseDto("Китайский Юань", new BigDecimal("4.2"), CurrencyCode.CNY),
                new ExchangeRateResponseDto("Российский Рубль", new BigDecimal("1.0"), CurrencyCode.RUB));
        wireMockServerExchange.stubFor(WireMock.get(WireMock.urlPathEqualTo(fromPath("/api/rates").toUriString()))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(exchangeRates))));

        AccountTransferRequestDto accountTransferRequestDto = new AccountTransferRequestDto()
                .setTargetAmount(new BigDecimal("200.00"))
                .setSourceAmount(new BigDecimal("100.00"))
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR)
                .setTargetLogin(targetLogin);
        ErrorResponseDto errorResponseDto = new ErrorResponseDto("Недостаточно денег на счету");
        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(errorResponseDto))));


        TransferRequestDto transferRequestDto = new TransferRequestDto()
                .setSourceAmount(new BigDecimal("100.00"))
                .setTargetLogin(targetLogin)
                .setSourceCurrency(CurrencyCode.RUB)
                .setTargetCurrency(CurrencyCode.IRR);
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post()
                .uri(fromPath("/transfer/{login}").buildAndExpand(sourceLogin).toUriString())
                .body(Mono.just(transferRequestDto), TransferRequestDto.class)
                .exchange()
                .expectStatus().isBadRequest();

        wireMockServerBlocker.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo("/operations/available/transfer"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(operationCheckRequestDto))));

        wireMockServerExchange.verify(1, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/rates"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE)));

        wireMockServerAccount.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(fromPath("/account/{login}/operation/transfer").buildAndExpand(sourceLogin).toUriString()))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountTransferRequestDto))));


        DeferredNotificationEntity expectedEntity = new DeferredNotificationEntity()
                .setStatus(MessageStatus.CREATED.toString())
                .setLogin(sourceLogin)
                .setMessage("Ошибка операции: Недостаточно денег на счету");
        StepVerifier.create(deferredNotificationRepository.findAll()
                        .collectList())
                .assertNext(deferredNotificationEntities -> {
                    Assertions.assertThat(deferredNotificationEntities).hasSize(1);
                    DeferredNotificationEntity notificationEntity = deferredNotificationEntities.stream().findFirst().get();
                    Assertions.assertThat(expectedEntity)
                            .usingRecursiveComparison()
                            .ignoringFields("id", "createdAt")
                            .isEqualTo(notificationEntity);
                    Assertions.assertThat(notificationEntity.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }
}
