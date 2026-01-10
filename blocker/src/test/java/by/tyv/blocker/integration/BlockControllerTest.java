package by.tyv.blocker.integration;

import by.tyv.blocker.enums.CashAction;
import by.tyv.blocker.enums.CurrencyCode;
import by.tyv.blocker.model.dto.BlockerResponseDto;
import by.tyv.blocker.model.dto.OperationCashRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class BlockControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("POST /operations/available/cash проверка операции списания/зачисления")
    public void testBlockCashThenTrue() {
        OperationCashRequestDto operationCashRequestDto = new  OperationCashRequestDto()
                .setAction(CashAction.GET)
                .setCurrency(CurrencyCode.BYN)
                .setAmount(new BigDecimal("100.00"));
        webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri("/operations/available/cash")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(operationCashRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BlockerResponseDto.class)
                .consumeWith(blockerResponse ->
                        Assertions.assertThat(blockerResponse.getResponseBody()).isNotNull());
    }
}
