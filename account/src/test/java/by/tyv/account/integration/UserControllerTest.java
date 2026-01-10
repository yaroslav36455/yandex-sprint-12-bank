package by.tyv.account.integration;

import by.tyv.account.SpringBootIntegrationTest;
import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.model.bo.SignUpForm;
import by.tyv.account.model.dto.EditAccountsDto;
import by.tyv.account.model.dto.ErrorResponseDto;
import by.tyv.account.model.dto.PasswordUpdateDto;
import by.tyv.account.model.dto.SignUpFormDto;
import by.tyv.account.model.entity.AccountEntity;
import by.tyv.account.model.entity.UserEntity;
import by.tyv.account.repository.UserRepository;
import by.tyv.account.util.TestUtils;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

public class UserControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql"})
    @DisplayName("GET /user, успешное чтение всех пользователей")
    public void getUsers() throws Exception {
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .get().uri(fromPath("/user").toUriString())
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(TestUtils.readResource("json/user_response.json"));
    }

    @Test
    @Sql({"/sql/clean.sql"})
    @DisplayName("GET /user, успешное чтение всех пользователей, пользователи отсутствуют")
    public void getUsersNoAnyUser() {
        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "some-subject")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .get().uri(fromPath("/user").toUriString())
                .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }

    @Test
    @Sql({"/sql/clean.sql"})
    @DisplayName("POST /signup, успешное создание нового пользователя, возвращение статуса 200")
    public void createNewUser() {
        SignUpFormDto signUpFormDto = new SignUpFormDto()
                .setName("Maria")
                .setLogin("someLogin_1")
                .setPassword("somePassword_1")
                .setConfirmPassword("somePassword_1")
                .setBirthDate(LocalDate.of(1988, 8, 12));
        UserEntity expecetdUserEntity = new UserEntity()
                .setId(1L)
                .setSub("user-sub")
                .setName("Maria")
                .setLogin("someLogin_1")
                .setBirthDate(LocalDate.of(1988, 8, 12));

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                  {
                                    "access_token": "test-admin-token",
                                    "expires_in": 300,
                                    "token_type": "Bearer"
                                  }
                                  """)));

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/admin/realms/test-realm/users"))
                .withHeader("Authorization", WireMock.equalTo("Bearer test-admin-token"))
                .willReturn(WireMock.created().withHeader(HttpHeaders.LOCATION, "http://some-host/" + expecetdUserEntity.getSub())));
        wireMockServerKeycloak.stubFor(WireMock.put(WireMock.urlMatching("/admin/realms/test-realm/users/.+/reset-password"))
                .withHeader("Authorization", WireMock.equalTo("Bearer test-admin-token"))
                .willReturn(WireMock.noContent()));

        webClient.post().uri(fromPath("/signup").toUriString())
                .bodyValue(signUpFormDto)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isCreated();

        StepVerifier.create(userRepository.findAll().collectList())
                .assertNext(userEntities -> {
                    Assertions.assertThat(userEntities)
                            .singleElement()
                            .usingRecursiveComparison()
                            .ignoringFields("createdAt", "password")
                            .isEqualTo(expecetdUserEntity);
                    Assertions.assertThat(userEntities)
                            .singleElement()
                            .satisfies(userEntity -> {
                                        Assertions.assertThat(userEntity.getId()).isNotNull();
                                        Assertions.assertThat(userEntity.getCreatedAt()).isNotNull();
                                    }
                            );
                })
                .verifyComplete();

        wireMockServerKeycloak.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/realms/test-realm/protocol/openid-connect/token")));
        wireMockServerKeycloak.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/admin/realms/test-realm/users"))
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer test-admin-token")));
        wireMockServerKeycloak.verify(WireMock.putRequestedFor(WireMock.urlPathEqualTo("/admin/realms/test-realm/users/%s/reset-password".formatted(expecetdUserEntity.getSub())))
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer test-admin-token")));
    }

    @Test
    @Sql("/sql/clean.sql")
    @DisplayName("POST /signup, попытка создания нового пользователя, пользователь с таким логином уже существует")
    public void createNewUserButUserAlreadyExists() {
        SignUpForm signUpForm = new SignUpForm()
                .setName("Maria")
                .setLogin("someLogin_1")
                .setPassword("somePassword_2")
                .setConfirmPassword("somePassword_2")
                .setBirthDate(LocalDate.of(1989, 9, 13));

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                  {
                                    "access_token": "test-admin-token",
                                    "expires_in": 300,
                                    "token_type": "Bearer"
                                  }
                                  """)));

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/admin/realms/test-realm/users"))
                .withHeader("Authorization", WireMock.equalTo("Bearer test-admin-token"))
                .willReturn(WireMock.status(HttpStatus.CONFLICT.value())));

        webClient.post().uri(fromPath("/signup").toUriString())
                .bodyValue(signUpForm)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponseDto.class)
                .consumeWith(result -> {
                    Assertions.assertThat(result.getResponseBody()).isNotNull();
                    Assertions.assertThat(result.getResponseBody().getErrorMessage()).isEqualTo("Логин 'someLogin_1' уже используется");
                });
    }

    @Test
    @Sql("/sql/clean.sql")
    @DisplayName("POST /signup, попытка создания нового пользователя, тело не проходит валидацию из-за несовпадения паролей")
    public void createNewUserButUserHasIllegalCreatedFields() {
        SignUpForm  signUpForm = new SignUpForm()
                .setName("Maria")
                .setLogin("someLogin_1")
                .setPassword("somePassword_2")
                .setConfirmPassword("somePassword_thatIsNotEqualsToPassword")
                .setBirthDate(LocalDate.of(1989, 9, 13));

        webClient.post().uri(fromPath("/signup").toUriString())
                .bodyValue(signUpForm)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponseDto.class)
                .consumeWith(result -> {
                    Assertions.assertThat(result.getResponseBody()).isNotNull();
                    Assertions.assertThat(result.getResponseBody().getErrorMessage()).isEqualTo("Проверочный пароль не совпадает");
                });
    }


    @Test
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql"})
    @DisplayName("POST /user/{login}/editPassword, изменение пароля пользователю, 200 OK")
    public void updateUserPasswordSuccessful() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto("newPassword", "newPassword");

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                  {
                                    "access_token": "test-admin-token",
                                    "expires_in": 300,
                                    "token_type": "Bearer"
                                  }
                                  """)));

        wireMockServerKeycloak.stubFor(WireMock.put(WireMock.urlEqualTo("/admin/realms/test-realm/users/cfca6cc3-174d-45d9-84b0-8b296b3f43f0/reset-password"))
                .withHeader("Authorization", WireMock.equalTo("Bearer test-admin-token"))
                .willReturn(WireMock.noContent()));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "cfca6cc3-174d-45d9-84b0-8b296b3f43f0")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/user/{login}/editPassword").buildAndExpand("SomeLogin").toUriString())
                .bodyValue(passwordUpdateDto)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk();

        wireMockServerKeycloak.verify(WireMock.postRequestedFor(WireMock.urlPathEqualTo("/realms/test-realm/protocol/openid-connect/token")));
        wireMockServerKeycloak.verify(WireMock.putRequestedFor(WireMock.urlPathEqualTo("/admin/realms/test-realm/users/cfca6cc3-174d-45d9-84b0-8b296b3f43f0/reset-password"))
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer test-admin-token")));
    }

    @Test
    @Sql("/sql/clean.sql")
    @DisplayName("POST /user/{login}/editPassword, изменение пароля пользователю неуспешно, пользователь не найден, 400 OK")
    public void updateUserPasswordUnsuccessful() {
        PasswordUpdateDto passwordUpdateDto = new PasswordUpdateDto("newPassword", "newPassword");

        wireMockServerKeycloak.stubFor(WireMock.post(WireMock.urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(WireMock.aResponse()
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                  {
                                    "access_token": "test-admin-token",
                                    "expires_in": 300,
                                    "token_type": "Bearer"
                                  }
                                  """)));

        wireMockServerKeycloak.stubFor(WireMock.put(WireMock.urlEqualTo("/admin/realms/test-realm/users/cfca6cc3-174d-45d9-84b0-8b296b3f43f0/reset-password"))
                .withHeader("Authorization", WireMock.equalTo("Bearer test-admin-token"))
                .willReturn(WireMock.noContent()));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "cfca6cc3-174d-45d9-84b0-8b296b3f43f0")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/user/{login}/editPassword").buildAndExpand("SomeLogin").toUriString())
                .bodyValue(passwordUpdateDto)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ErrorResponseDto.class)
                .value(errorResponseDto -> Assertions.assertThat(errorResponseDto.getErrorMessage())
                        .isEqualTo("Пользователь 'SomeLogin' не найден"));
    }

    @Test
    @Sql({"/sql/clean.sql", "/sql/insert_users.sql", "/sql/insert_accounts.sql"})
    @DisplayName("POST /user/{login}/editUserAccounts - изменение аккаунтов пользователя, статус 200 OK")
    public void updateUserAccounts() {
        EditAccountsDto editAccountsDto = new EditAccountsDto()
                .setName("Some New Name")
                .setBirthDate(LocalDate.of(2000, 1, 1))
                .setAccounts(List.of(CurrencyCode.RUB, CurrencyCode.INR, CurrencyCode.CNY));

        webClient.mutateWith(mockJwt().jwt(jwt -> jwt
                        .claim("sub", "cfca6cc3-174d-45d9-84b0-8b296b3f43f0")
                        .claim("client_id", "some-client-id")
                        .claim("scope", "internal_call")))
                .post().uri(fromPath("/user/{login}/editUserAccounts").buildAndExpand("SomeLogin").toUriString())
                .bodyValue(editAccountsDto)
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody().isEmpty();

        StepVerifier.create(userRepository.findByLogin("SomeLogin"))
                .assertNext(user -> {
                    Assertions.assertThat(user).isNotNull();
                    Assertions.assertThat(user.getName()).isEqualTo("Some New Name");
                    Assertions.assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
                })
                .verifyComplete();

        StepVerifier.create(accountRepository.findAllByLogin("SomeLogin").collectList())
                .assertNext(accounts -> {
                    var expected = List.of(
                            new AccountEntity().setBalance(new BigDecimal("0.00")).setCurrency(CurrencyCode.INR),
                            new AccountEntity().setBalance(new BigDecimal("2000.00")).setCurrency(CurrencyCode.RUB),
                            new AccountEntity().setBalance(new BigDecimal("0.00")).setCurrency(CurrencyCode.CNY)
                    );

                    Assertions.assertThat(accounts).isNotNull();
                    Assertions.assertThat(accounts)
                            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "userId")
                            .containsExactlyInAnyOrderElementsOf(expected);
                })
                .verifyComplete();
    }
}
