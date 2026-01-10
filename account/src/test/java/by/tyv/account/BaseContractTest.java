package by.tyv.account;

import by.tyv.account.config.SecurityConfiguration;
import by.tyv.account.controller.AccountController;
import by.tyv.account.controller.UserController;
import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.mapper.AccountMapperImpl;
import by.tyv.account.mapper.UserMapperImpl;
import by.tyv.account.model.bo.*;
import by.tyv.account.service.AccountService;
import by.tyv.account.service.UserService;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = {AccountController.class, UserController.class})
@Import({AccountMapperImpl.class, UserMapperImpl.class, SecurityConfiguration.class})
@AutoConfigureWebTestClient
public class BaseContractTest {
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private UserService userService;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    public void beforeEach() {
        Mockito.doReturn(Mono.empty())
                .when(accountService)
                .cashOperation(Mockito.any(OperationCash.class));

        Mockito.doReturn(Mono.empty())
                .when(accountService)
                .transferOperation(Mockito.any(OperationTransfer.class));

        Mockito.doReturn(Flux.fromIterable(List.of(
                new AccountInfo()
                        .setId(1234L)
                        .setCreatedAt(LocalDateTime.of(2000, 1, 1, 5, 12, 59, 567344000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("2000.00"))
                        .setCurrency(CurrencyCode.BYN),
                new AccountInfo()
                        .setId(8888L)
                        .setCreatedAt(LocalDateTime.of(1999, 12, 9, 17, 9, 15, 847234000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("18066.12"))
                        .setCurrency(CurrencyCode.RUB),
                new AccountInfo()
                        .setId(9876L)
                        .setCreatedAt(LocalDateTime.of(2018, 11, 16, 22, 45, 3, 656234000))
                        .setUserId(4567L)
                        .setBalance(new BigDecimal("6788.55"))
                        .setCurrency(CurrencyCode.CNY)
                )))
                .when(accountService)
                .getAccounts(Mockito.any(String.class));

        Mockito.doReturn(Flux.fromIterable(List.of(
                new UserInfo()
                        .setId(1234L)
                        .setCreatedAt(LocalDateTime.of(2000, 1, 1, 5, 12, 59, 567344000))
                        .setLogin("someLogin_1")
                        .setName("Maria")
                        .setBirthDate(LocalDate.of(1988, 8, 12)),
                new UserInfo()
                        .setId(8888L)
                        .setCreatedAt(LocalDateTime.of(1999, 12, 9, 17, 9, 15, 847234000))
                        .setLogin("someLogin_2")
                        .setName("Zlata")
                        .setBirthDate(LocalDate.of(2005, 12, 8)),
                new UserInfo()
                        .setId(9876L)
                        .setCreatedAt(LocalDateTime.of(2018, 11, 16, 22, 45, 3, 656234000))
                        .setLogin("someLogin_3")
                        .setName("Yaroslav")
                        .setBirthDate(LocalDate.of(1995, 9, 23))
                )))
                .when(userService)
                .getUsers();

        Mockito.doReturn(Mono.empty())
                .when(userService)
                .signUp(Mockito.any(SignUpForm.class));

        Mockito.doReturn(Mono.empty())
                .when(userService)
                .updatePassword(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class));

        RestAssuredWebTestClient.webTestClient(webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt
                .claim("sub", "some-subject")
                .claim("client_id", "some-client-id")
                .claim("scope", "internal_call"))));
    }
}
