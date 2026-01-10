package by.tyv.frontui.service.impl;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.exception.ServiceException;
import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.dto.OperationCashRequestDto;
import by.tyv.frontui.model.dto.TransferRequestDto;
import by.tyv.frontui.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrontUiServiceImpl implements FrontUiService {
    private final AccountService accountService;
    private final TransferService transferService;
    private final CashService cashService;
    private final TokenProvider tokenProvider;

    @Override
    public Mono<Rendering> buildMainPage() {
        return tokenProvider.getCurrentUsername().flatMap(username -> Mono.zip(
                        Mono.from(accountService.getUserAccounts(username).collectList()),
                        Mono.from(accountService.getUsers().collectList()))
                .map(tuple -> Rendering.view("main")
                        .modelAttribute("accounts", tuple.getT1())
                        .modelAttribute("currency", List.of(CurrencyCode.values()))
                        .modelAttribute("users", tuple.getT2()
                                .stream().filter(user -> !Objects.equals(user.getLogin(), username)).toList())
                        .modelAttribute("currentUser", tuple.getT2()
                                .stream().filter(user -> Objects.equals(user.getLogin(), username)).findFirst()
                                .orElseThrow(() -> new ServiceException("Не найден текущий пользователь")))
                        .build()));
    }

    @Override
    public Mono<Rendering> signUp(SignUpForm signUpForm) {
        return accountService.createUser(signUpForm)
                .thenReturn(Rendering.redirectTo("/oauth2/authorization/user-token-ac").build())
                .onErrorResume(ServiceException.class, e ->
                        Mono.just(Rendering.view("signup")
                                .modelAttribute("name", signUpForm.getName())
                                .modelAttribute("login", signUpForm.getLogin())
                                .modelAttribute("birthDate", signUpForm.getBirthDate())
                                .modelAttribute("errors", List.of(e.getMessage()))
                                .build()
                        )
                );
    }

    @Override
    public Mono<Rendering> updatePassword(String login, String password, String confirmPassword) {
        return accountService.updatePassword(login, password, confirmPassword)
                .thenReturn(Rendering.redirectTo("/main").build())
                .onErrorResume(ServiceException.class, error -> buildPageWithError("passwordErrors", error));
    }

    @Override
    public Mono<Rendering> updateAccounts(String login, String name, LocalDate birthdate, List<CurrencyCode> accounts) {
        return accountService.updateAccounts(login, name, birthdate, accounts)
                .thenReturn(Rendering.redirectTo("/main").build())
                .onErrorResume(ServiceException.class, error ->buildPageWithError("userAccountsErrors", error));
    }

    @Override
    public Mono<Rendering> cashOperation(String login, OperationCashRequestDto cashRequestDto) {
        return cashService.cashOperation(login, cashRequestDto)
                .thenReturn(Rendering.redirectTo("/main").build())
                .onErrorResume(ServiceException.class, error ->buildPageWithError("cashErrors", error));
    }

    @Override
    public Mono<Rendering> makeTransfer(String login, TransferRequestDto transferRequestDto) {
        log.info("XXX TransferRequestDto: {}", transferRequestDto);
        return transferService.makeTransfer(login, transferRequestDto)
                .thenReturn(Rendering.redirectTo("/main").build())
                .onErrorResume(ServiceException.class, error ->buildPageWithError("transferErrors", error));
    }

    private Mono<Rendering> buildPageWithError(String errorFieldName, ServiceException serviceException) {
        return tokenProvider.getCurrentUsername().flatMap(username -> Mono.zip(
                        Mono.from(accountService.getUserAccounts(username).collectList()),
                        Mono.from(accountService.getUsers().collectList()))
                .map(tuple -> Rendering.view("main")
                        .modelAttribute(errorFieldName, serviceException.getMessage())
                        .modelAttribute("accounts", tuple.getT1())
                        .modelAttribute("currency", List.of(CurrencyCode.values()))
                        .modelAttribute("users", tuple.getT2()
                                .stream().filter(user -> !Objects.equals(user.getLogin(), username)).toList())
                        .modelAttribute("currentUser", tuple.getT2()
                                .stream().filter(user -> Objects.equals(user.getLogin(), username)).findFirst()
                                .orElseThrow(() -> new ServiceException("Не найден текущий пользователь")))
                        .build()));
    }
}
