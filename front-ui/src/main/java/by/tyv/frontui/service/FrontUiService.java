package by.tyv.frontui.service;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.dto.OperationCashRequestDto;
import by.tyv.frontui.model.dto.TransferRequestDto;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface FrontUiService {
    Mono<Rendering> buildMainPage();
    Mono<Rendering> signUp(SignUpForm signUpForm);
    Mono<Rendering> updatePassword(String login, String password, String confirmPassword);
    Mono<Rendering> updateAccounts(String login, String name, LocalDate birthdate, List<CurrencyCode> accounts);
    Mono<Rendering> cashOperation(String login, OperationCashRequestDto cashRequestDto);
    Mono<Rendering> makeTransfer(String login, TransferRequestDto transferRequestDto);
}
