package by.tyv.account.service.impl;

import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.exception.AccountException;
import by.tyv.account.exception.UserNotFoundException;
import by.tyv.account.mapper.UserMapper;
import by.tyv.account.model.bo.EditAccounts;
import by.tyv.account.model.bo.SignUpForm;
import by.tyv.account.model.bo.UserInfo;
import by.tyv.account.model.entity.AccountEntity;
import by.tyv.account.model.entity.UserEntity;
import by.tyv.account.repository.AccountRepository;
import by.tyv.account.repository.UserRepository;
import by.tyv.account.service.TokenProvider;
import by.tyv.account.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    static private final Pattern loginPattern = Pattern.compile("^([A-Za-z0-9._-]+)$");
    static private final Pattern namePattern = Pattern.compile("^([A-Za-z\\s-]+)$");
    static private final long minPasswordLength = 6;
    static private final long minAge = 18;

    private final WebClient keycloakWebClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final String keycloakRealm;
    private final TokenProvider tokenProvider;
    private final TransactionalOperator transactionalOperator;
    private final AccountRepository accountRepository;

    public UserServiceImpl(@Value("${clients.keycloak.url}") String keycloakClientUrl,
                           WebClient.Builder webClientBuilder,
                           UserRepository userRepository,
                           UserMapper userMapper,
                           @Value("${clients.keycloak.realm}") String keycloakRealm,
                           TokenProvider tokenProvider,
                           TransactionalOperator transactionalOperator, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.keycloakWebClient = webClientBuilder.baseUrl(keycloakClientUrl).build();
        this.keycloakRealm = keycloakRealm;
        this.tokenProvider = tokenProvider;
        this.transactionalOperator = transactionalOperator;
        this.accountRepository = accountRepository;
    }

    @Override
    public Flux<UserInfo> getUsers() {
        return userRepository.findAll()
                .map(userMapper::toBO);
    }

    @Override
    public Mono<Void> signUp(SignUpForm signUpForm) {
        validate(signUpForm);
        return tokenProvider.getNewUserManagmentToken()
                .flatMap(token -> createUser(signUpForm.getLogin(), token)
                        .flatMap(sub -> setPassword(token, sub, signUpForm.getPassword())
                                .thenReturn(sub)))
                .map(sub -> new UserEntity()
                        .setSub(sub)
                        .setLogin(signUpForm.getLogin())
                        .setName(signUpForm.getName())
                        .setBirthDate(signUpForm.getBirthDate()))
                .flatMap(userRepository::save)
                .then();
    }

    @Override
    public Mono<Void> updatePassword(String login, String password, String confirmPassword) {
        if (!Objects.equals(password, confirmPassword)) {
            return Mono.error(new IllegalArgumentException("Не совпадают пароли указанные пользователем %s".formatted(login)))
                    .doOnError(error -> log.warn("Не совпадают пароли указанные пользователем {}", login, error))
                    .then();
        }

        return Mono.zip(tokenProvider.getNewUserManagmentToken(), this.findUserByLogin(login))
                .flatMap(tuple -> setPassword(tuple.getT1(), tuple.getT2().getSub(), password))
                .doOnSuccess(v -> log.info("Успешно изменён пароль для пользователя login={}", login));
    }

    private Mono<String> createUser(String login, String userManagementToken) {
        return keycloakWebClient.post()
                .uri("/admin/realms/{keycloakRealm}/users", keycloakRealm)
                .headers(headers -> headers.setBearerAuth(userManagementToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", login,
                        "enabled", true))
                .exchangeToMono(resp -> {
                    int sc = resp.statusCode().value();
                    if (sc == HttpStatus.CREATED.value()) {
                        var loc = resp.headers().asHttpHeaders().getLocation();
                        if (Objects.isNull(loc)) {
                            return Mono.error(new IllegalStateException("Не найден Location header в ответе на запрос"));
                        };
                        return Mono.just(loc.getPath().substring(loc.getPath().lastIndexOf('/') + 1));
                    } else if (sc == HttpStatus.CONFLICT.value()) {
                        return Mono.error(new RuntimeException("Логин '%s' уже используется".formatted(login)));
                    }
                    return resp.createException().flatMap(Mono::error);
                })
                .doOnNext(bodyLessEntity -> log.info("Создан пользователь login={} в realm={}", login, keycloakRealm))
                .doOnError(error -> log.error("Ошибка создания пользователя" , error));
    }

    private Mono<Void> setPassword(String token, String sub, String password) {
        return keycloakWebClient.put()
                .uri("/admin/realms/{keycloakRealm}/users/{sub}/reset-password", keycloakRealm, sub)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "type","password",
                        "value",password,
                        "temporary",false
                ))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Ошибка смены пароля в Keycloak sub={}, status={}, body={}",
                                            sub, resp.statusCode(), body);
                                    return Mono.error(new IllegalStateException(
                                            "Ошибка смены пароля в Keycloak, статус " + resp.statusCode()));
                                })
                )
                .toBodilessEntity()
                .doOnNext(bodyLessEntity -> log.info("Установлен новый пароль пользователю sub={} в realm={}", sub, keycloakRealm))
                .then();
    }

    private void validate(SignUpForm form) {
        String errorMessage = null;
        if (!loginPattern.matcher(form.getLogin()).matches()) {
            errorMessage = "Логин не соответствует шаблону [A-Za-z0-9._-]+";
        } else if (Objects.isNull(form.getPassword()) || form.getPassword().length() < minPasswordLength) {
            errorMessage = "Длина пароля меньше %d символов".formatted(minPasswordLength);
        } else if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            errorMessage = "Проверочный пароль не совпадает";
        } else if (!namePattern.matcher(form.getName()).matches()) {
            errorMessage = "Имя не соответствует шаблону [A-Za-z\\s-]+";
        } else if (Objects.isNull(form.getBirthDate())) {
            errorMessage = "Не указана дата рождения";
        } else if (LocalDate.now().minusYears(minAge).isBefore(form.getBirthDate())) {
            errorMessage = "Дата рождения %s - меньше допустимого возраста (%d лет)".formatted(form.getName(), minAge);
        }

        if (Objects.nonNull(errorMessage)) {
            log.error(errorMessage);
            throw new AccountException(errorMessage);
        }
    }

    private Mono<UserEntity> findUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .switchIfEmpty(Mono.error(new UserNotFoundException("Пользователь '%s' не найден".formatted(login))))
                .doOnError(throwable -> log.warn("User '{}' not found", login));
    }

    @Override
    public Mono<Void> updateAccounts(String login, EditAccounts editAccounts) {
        validate(editAccounts);
        return userRepository.findByLogin(login)
                .map(user -> {
                    user.setName(Objects.nonNull(editAccounts.getName()) ? editAccounts.getName() : user.getName());
                    user.setBirthDate(Objects.nonNull(editAccounts.getBirthDate()) ? editAccounts.getBirthDate() : user.getBirthDate());
                    return user;
                })
                .flatMap(userRepository::save)
                .flatMap(userEntity -> accountRepository.findAllByLogin(userEntity.getLogin())
                        .collectList()
                        .flatMap((Function<List<AccountEntity>, Mono<Void>>) oldAccountEntities -> {
                            List<CurrencyCode> oldCurrencyList = oldAccountEntities.stream()
                                    .map(AccountEntity::getCurrency)
                                    .toList();

                            List<CurrencyCode> newCurrencies = Objects.isNull(editAccounts.getAccounts())
                                    ? new ArrayList<>()
                                    : editAccounts.getAccounts();
                            List<AccountEntity> accountsToCreate = newCurrencies.stream()
                                    .filter(Predicate.not(oldCurrencyList::contains))
                                    .map(currencyCode -> new AccountEntity()
                                            .setUserId(userEntity.getId())
                                            .setCurrency(currencyCode)
                                            .setBalance(BigDecimal.ZERO))
                                    .toList();
                            List<AccountEntity> accountsToRemove = oldAccountEntities.stream()
                                    .filter(Predicate.not(accountEntity -> newCurrencies.contains(accountEntity.getCurrency())))
                                    .toList();

                            return accountRepository.deleteAll(accountsToRemove)
                                    .thenMany(accountRepository.saveAll(accountsToCreate))
                                    .then();
                        }))
                .as(transactionalOperator::transactional)
                .then();
    }

    private void validate(EditAccounts editAccounts) {
        String errorMessage = null;
        if (Objects.nonNull(editAccounts.getName()) && !namePattern.matcher(editAccounts.getName()).matches()) {
            errorMessage = "Имя не соответствует шаблону [A-Za-z\\s-]+";
        } else if (Objects.nonNull(editAccounts.getBirthDate()) && LocalDate.now().minusYears(minAge).isBefore(editAccounts.getBirthDate())) {
            errorMessage = "Дата рождения %s - меньше допустимого возраста (%d лет)".formatted(editAccounts.getBirthDate(), minAge);
        }

        if (Objects.nonNull(errorMessage)) {
            log.error(errorMessage);
            throw new AccountException(errorMessage);
        }
    }
}
