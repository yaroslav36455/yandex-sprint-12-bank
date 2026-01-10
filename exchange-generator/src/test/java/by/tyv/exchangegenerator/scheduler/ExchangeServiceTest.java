package by.tyv.exchangegenerator.scheduler;

import by.tyv.exchangegenerator.SpringBootIntegrationTest;
import by.tyv.exchangegenerator.enums.CurrencyCode;
import by.tyv.exchangegenerator.model.bo.ExchangeRate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeServiceTest extends SpringBootIntegrationTest {

    @Test
    @DisplayName("Обновление курсов валют")
    public void testExchangeService() throws JsonProcessingException {
        List<ExchangeRate> expectedList = List.of(
                new ExchangeRate(CurrencyCode.RUB, BigDecimal.valueOf(1.0)),
                new ExchangeRate(CurrencyCode.BYN, BigDecimal.valueOf(10.0)),
                new ExchangeRate(CurrencyCode.IRR, BigDecimal.valueOf(20.0)),
                new ExchangeRate(CurrencyCode.CNY, BigDecimal.valueOf(30.0)),
                new ExchangeRate(CurrencyCode.INR, BigDecimal.valueOf(40.0))
        );

        String requestBody = objectMapper.writeValueAsString(expectedList);

        wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/api/update"))
                        .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                        .withRequestBody(WireMock.equalToJson(requestBody))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        StepVerifier.create(exchangeService.update(expectedList))
                .verifyComplete();

        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/api/update"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(requestBody))
        );
    }
}
