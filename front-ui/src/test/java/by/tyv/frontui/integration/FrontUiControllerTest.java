package by.tyv.frontui.integration;

import by.tyv.frontui.SpringBootIntegrationTest;
import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;


public class FrontUiControllerTest extends SpringBootIntegrationTest {

    @Test
    @DisplayName("GET / - редирект на /main")
    public void redirectToMainPage() {
        webClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/main");
    }

    @Test
    @DisplayName("GET /main возвращение основной страницы")
    public void getMainPage() throws JsonProcessingException {
        List<AccountInfoDto> accountInfo = List.of(
                new AccountInfoDto()
                        .setId(2L)
                        .setUserId(7L)
                        .setCurrency(CurrencyCode.BYN)
                        .setBalance(new BigDecimal("66.55"))
                        .setCreatedAt(LocalDateTime.of(2021, 5, 4, 12, 12)),
                new AccountInfoDto()
                        .setId(1L)
                        .setUserId(7L)
                        .setCurrency(CurrencyCode.RUB)
                        .setBalance(new BigDecimal("2000.05"))
                        .setCreatedAt(LocalDateTime.of(2021, 5, 5, 0, 0)),
                new AccountInfoDto()
                        .setId(5L)
                        .setUserId(7L)
                        .setCurrency(CurrencyCode.CNY)
                        .setBalance(new BigDecimal("140.40"))
                        .setCreatedAt(LocalDateTime.of(2024, 12, 11, 0, 6))
        );
        wireMockServerAccount.stubFor(WireMock.get(WireMock.urlPathEqualTo("/account/andrew"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(accountInfo))));

        List<UserInfoDto> userInfos = List.of(
                new UserInfoDto()
                        .setId(1L)
                        .setName("Andrew")
                        .setLogin("andrew")
                        .setBirthDate(LocalDate.of(2000, 1, 1))
                        .setCreatedAt(LocalDateTime.of(2021, 5, 4, 12, 2)),
                new UserInfoDto()
                        .setId(2L)
                        .setName("Maria")
                        .setLogin("maria")
                        .setBirthDate(LocalDate.of(2010, 2, 11))
                        .setCreatedAt(LocalDateTime.of(2021, 5, 4, 12, 2)),
                new UserInfoDto()
                        .setId(4L)
                        .setName("Roman")
                        .setLogin("roman")
                        .setBirthDate(LocalDate.of(1991, 9, 26))
                        .setCreatedAt(LocalDateTime.of(2024, 7, 12, 18, 13))
        );
        wireMockServerAccount.stubFor(WireMock.get(WireMock.urlPathEqualTo("/user"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(userInfos))));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "user")
                        .claim("preferred_username", "andrew")))
                .get()
                .uri("/main")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<td style=\"font-weight:bold;\">Белорусский Рубль</td>");
                    assertThat(html).contains("<td style=\"font-weight:bold;\">Российский Рубль</td>");
                    assertThat(html).contains("<td style=\"font-weight:bold;\">Китайский Юань</td>");

                    assertThat(html).contains("<form method=\"post\" action=\"/user/andrew/cash\">");
                    assertThat(html).contains("<td style=\"font-weight:bold;\">Фамилия Имя</td>");
                    assertThat(html).contains("<td>Andrew</td>");
                    assertThat(html).contains("<input hidden name=\"targetLogin\" value=\"andrew\"/>");
                    assertThat(html).contains("<option value=\"maria\">Maria</option>");
                    assertThat(html).contains("<option value=\"roman\">Roman</option>");
                });
    }

    @Test
    @DisplayName("GET /signup - страница регистрации нового пользователя")
    public void redirectToSignupPage() {
        webClient.get()
                .uri("/signup")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("<title>Регистрация</title>");
                    assertThat(html).contains("Все поля обязательны для заполнения");
                });
    }

    @Test
    @DisplayName("POST /signup - успешное создание пользователя, редирект на /main")
    public void createNewUserSuccessfulAndRedirectToMainPage() throws JsonProcessingException {
        SignUpFormDto signUpFormDto = new SignUpFormDto()
                .setName("TestName")
                .setLogin("TestLogin")
                .setPassword("TestPassword")
                .setConfirmPassword("TestPassword")
                .setBirthDate(LocalDate.of(1990, 1,1));
        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo("/signup"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(signUpFormDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.CREATED.value())));

        webClient.post()
                .uri("/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("name", "TestName")
                        .with("login", "TestLogin")
                        .with("password", "TestPassword")
                        .with("confirmPassword", "TestPassword")
                        .with("birthDate", "1990-01-01"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/oauth2/authorization/user-token-ac$");
    }

    @Test
    @DisplayName("POST /signup - неуспешное создание пользователя, остаёмся на той же странице signup с заполненными полями и ошибками")
    public void tryCreateNewUserSuccessfulAndLeaveSignUpPage() throws JsonProcessingException {
        SignUpForm signUpForm = new SignUpForm()
                .setName("TestName")
                .setLogin("TestLogin")
                .setPassword("TestPassword")
                .setConfirmPassword("TestPassword")
                .setBirthDate(LocalDate.of(1990, 1,1));
        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo("/signup"))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(signUpForm)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(new ErrorResponseDto("Некорректный логин")))));

        webClient.post()
                .uri("/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("name", "TestName")
                        .with("login", "TestLogin")
                        .with("password", "TestPassword")
                        .with("confirmPassword", "TestPassword")
                        .with("birthDate", "1990-01-01"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("Некорректный логин");
                    assertThat(html).contains("name=\"login\"");
                    assertThat(html).contains("value=\"TestLogin\"");
                    assertThat(html).contains("name=\"name\"");
                    assertThat(html).contains("value=\"TestName\"");
                    assertThat(html).contains("name=\"birthDate\"");
                    assertThat(html).contains("value=\"1990-01-01\"");
                });
    }

    @Test
    @DisplayName("POST /user/{login}/editUserAccounts - обновление аккаунтов пользователя")
    public void updateUserAccounts() throws JsonProcessingException {
        AccountsUpdateDto accountsUpdateDto = new AccountsUpdateDto()
                .setName("Andrew")
                .setBirthDate(LocalDate.of(1990, 1,1))
                .setAccounts(List.of(CurrencyCode.RUB, CurrencyCode.BYN, CurrencyCode.CNY));
        String uriString = UriComponentsBuilder.fromPath("/user/{login}/editUserAccounts")
                .buildAndExpand("andrew")
                .toUriString();

        wireMockServerAccount.stubFor(WireMock.post(WireMock.urlPathEqualTo(uriString))
                .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(accountsUpdateDto)))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "user")))
                .post()
                .uri(uriString)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("name", "Andrew")
                        .with("birthDate", "1990-01-01")
                        .with("account", "RUB")
                        .with("account", "BYN")
                        .with("account", "CNY"))
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().value(HttpHeaders.LOCATION, loc -> Assertions.assertThat(loc).endsWith("/main"));
    }
}
