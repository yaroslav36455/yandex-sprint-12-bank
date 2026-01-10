package by.tyv.frontui.service.impl;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.exception.ServiceException;
import by.tyv.frontui.mapper.AccountMapper;
import by.tyv.frontui.mapper.UserMapper;
import by.tyv.frontui.model.bo.Account;
import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.bo.User;
import by.tyv.frontui.model.dto.*;
import by.tyv.frontui.service.AccountService;
import by.tyv.frontui.service.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final WebClient accountWebClient;
    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final TokenProvider tokenProvider;

    public AccountServiceImpl(@Value("${clients.account-service.url}") String accountServiceUrl,
                              WebClient.Builder webClientBuilder,
                              AccountMapper accountMapper,
                              UserMapper userMapper,
                              TokenProvider tokenProvider) {
        this.accountWebClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Flux<Account> getUserAccounts(String username) {
        return tokenProvider.getNewTechnical()
                .flatMapMany(token -> accountWebClient.get()
                        .uri(uriBuilder -> uriBuilder.pathSegment("account", username).build())
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .retrieve()
                        .bodyToFlux(AccountInfoDto.class)
                        .map(accountMapper::toBO)
                        .collectList()
                        .flatMapMany(accounts -> {
                            List<Account> doesNotExistsAccounts = Stream.of(CurrencyCode.values())
                                    .filter(currencyCode -> accounts.stream().map(Account::getCurrency).noneMatch(currencyCode::equals))
                                    .map(currencyCode -> new Account(BigDecimal.ZERO, currencyCode, false))
                                    .toList();
                            accounts.addAll(doesNotExistsAccounts);
                            return Flux.fromIterable(accounts);
                        }));
    }

    @Override
    public Flux<User> getUsers() {
        return tokenProvider.getNewTechnical().flatMapMany(token ->
                accountWebClient.get()
                        .uri("/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .retrieve()
                        .bodyToFlux(UserInfoDto.class)
                        .map(userMapper::toBO));
    }

    @Override
    public Mono<Void> createUser(SignUpForm signUpForm) {
        return accountWebClient
                .post().uri("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(signUpForm), SignUpForm.class)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка создания пользователя", throwable));
    }

    @Override
    public Mono<Void> updatePassword(String login, String password, String confirmPassword) {
        return tokenProvider.getNewTechnical().flatMap(token -> accountWebClient.post()
                .uri(UriComponentsBuilder.fromPath("/user/{login}/editPassword").buildAndExpand(login).toString())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordUpdateDto(password,  confirmPassword))
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка обновления пароля", throwable)));
    }

    @Override
    public Mono<Void> updateAccounts(String login, String name, LocalDate birthdate, List<CurrencyCode> accounts) {
        return tokenProvider.getNewTechnical().flatMap(token -> accountWebClient.post()
                .uri(UriComponentsBuilder.fromPath("/user/{login}/editUserAccounts").buildAndExpand(login).toString())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AccountsUpdateDto().setName(name).setBirthDate(birthdate).setAccounts(accounts))
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка обновления списка аккаунтов", throwable)));
    }
}
