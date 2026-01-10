package by.tyv.frontui.service;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.model.bo.Account;
import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.bo.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface AccountService {
    Flux<Account> getUserAccounts(String username);
    Flux<User> getUsers();
    Mono<Void> createUser(SignUpForm signUpForm);
    Mono<Void> updatePassword(String login, String password, String confirmPassword);
    Mono<Void> updateAccounts(String login, String name, LocalDate birthdate, List<CurrencyCode> accounts);
}
