package by.tyv.frontui.integration;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.model.dto.AccountInfoDto;
import by.tyv.frontui.model.dto.SignUpFormDto;
import by.tyv.frontui.model.dto.UserInfoDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FrontUiConsumerStubRunnerTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .stubsMode(StubRunnerProperties.StubsMode.LOCAL)
            .downloadStub("by.tyv:account:1.0.0:stubs");

    private WebClient webClientAccount;

    @BeforeEach
    void setUp() {
        String accountBase  = stubRunner.findStubUrl("by.tyv", "account").toString();
        assertThat(accountBase).isNotBlank();
        webClientAccount = WebClient.builder().baseUrl(accountBase).build();
    }

    @Test
    @DisplayName("GET /account/{login}, получить список счетов пользователя и ответ 200")
    void callClientAccountGetUserAccountsSuccessTest() {
        List<AccountInfoDto> accountInfoDtoListExpected = List.of(
                new AccountInfoDto()
                        .setId(1234L)
                        .setCreatedAt(LocalDateTime.of(2000, 1, 1, 5, 12, 59, 567344000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("2000.00"))
                        .setCurrency(CurrencyCode.BYN),
                new AccountInfoDto()
                        .setId(8888L)
                        .setCreatedAt(LocalDateTime.of(1999, 12, 9, 17, 9, 15, 847234000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("18066.12"))
                        .setCurrency(CurrencyCode.RUB),
                new AccountInfoDto()
                        .setId(9876L)
                        .setCreatedAt(LocalDateTime.of(2018, 11, 16, 22, 45, 3, 656234000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("6788.55"))
                        .setCurrency(CurrencyCode.CNY));

        StepVerifier.create(
                        webClientAccount.get().uri("/account/sourceLogin")
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToFlux(AccountInfoDto.class)
                                .collectList()
                )
                .assertNext(accountInfoDtoListActual ->
                        Assertions.assertThat(accountInfoDtoListActual)
                                .usingRecursiveComparison()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .isEqualTo(accountInfoDtoListExpected))
                .verifyComplete();
    }

    @Test
    @DisplayName("GET /user, получить список пользователей и ответ 200")
    void callClientAccountGetUsersSuccessTest() {
        List<UserInfoDto> userInfoDtoListExpected = List.of(
                new UserInfoDto()
                        .setId(1234L)
                        .setCreatedAt(LocalDateTime.of(2000, 1, 1, 5, 12, 59, 567344000))
                        .setLogin("someLogin_1")
                        .setName("Maria")
                        .setBirthDate(LocalDate.of(1988, 8, 12)),
                new UserInfoDto()
                        .setId(8888L)
                        .setCreatedAt(LocalDateTime.of(1999, 12, 9, 17, 9, 15, 847234000))
                        .setLogin("someLogin_2")
                        .setName("Zlata")
                        .setBirthDate(LocalDate.of(2005, 12, 8)),
                new UserInfoDto()
                        .setId(9876L)
                        .setCreatedAt(LocalDateTime.of(2018, 11, 16, 22, 45, 3, 656234000))
                        .setLogin("someLogin_3")
                        .setName("Yaroslav")
                        .setBirthDate(LocalDate.of(1995, 9, 23))
        );

        StepVerifier.create(
                        webClientAccount.get().uri("/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToFlux(UserInfoDto.class)
                                .collectList()
                )
                .assertNext(userInfoDtoListActual ->
                        Assertions.assertThat(userInfoDtoListActual)
                                .usingRecursiveComparison()
                                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                                .isEqualTo(userInfoDtoListExpected))
                .verifyComplete();
    }

    @Test
    @DisplayName("POST /signup, создать нового пользователя, ответ 200")
    void callClientAccountPostNewUserSuccessTest() {
        SignUpFormDto signUpFormDto = new SignUpFormDto()
                .setLogin("someLogin_1")
                .setName("Maria")
                .setPassword("some_password1234")
                .setConfirmPassword("some_password1234")
                .setBirthDate(LocalDate.of(1988, 8, 12));
        StepVerifier.create(
                        webClientAccount.post().uri("/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(signUpFormDto)
                                .exchangeToMono(clientResponse -> {
                                    assertThat(clientResponse.statusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
                                    return Mono.empty();
                                })
                )
                .verifyComplete();
    }
}
