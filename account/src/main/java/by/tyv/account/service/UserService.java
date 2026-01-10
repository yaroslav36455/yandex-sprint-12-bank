package by.tyv.account.service;

import by.tyv.account.model.bo.EditAccounts;
import by.tyv.account.model.bo.SignUpForm;
import by.tyv.account.model.bo.UserInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {
    Flux<UserInfo> getUsers();
    Mono<Void> signUp(SignUpForm signUpForm);
    Mono<Void> updatePassword(String login, String password, String confirmPassword);
    Mono<Void> updateAccounts(String login, EditAccounts editAccounts);
}
