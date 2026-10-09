package by.tyv.account.service.impl;

import by.tyv.account.enums.CashAction;
import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.mapper.AccountMapper;
import by.tyv.account.model.bo.AccountInfo;
import by.tyv.account.model.bo.OperationCash;
import by.tyv.account.model.bo.OperationTransfer;
import by.tyv.account.repository.AccountRepository;
import by.tyv.account.service.AccountService;
import by.tyv.account.service.NotificationService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionalOperator transactionalOperator;
    private final NotificationService notificationService;
    private final AccountMapper accountMapper;
    private final Counter cashOperationFailedAmount;
    private final Counter transferOperationFailedAmount;

    public AccountServiceImpl(AccountRepository accountRepository,
                              TransactionalOperator transactionalOperator,
                              NotificationService notificationService,
                              AccountMapper accountMapper,
                              MeterRegistry registry) {
        this.accountRepository = accountRepository;
        this.transactionalOperator = transactionalOperator;
        this.notificationService = notificationService;
        this.accountMapper = accountMapper;
        this.cashOperationFailedAmount = Counter.builder("bank_cash_failed_total")
                .description("Количество неуспешных попыток операций с наличными")
                .register(registry);
        this.transferOperationFailedAmount = Counter.builder("bank_transfer_failed_total")
                .description("Количество неуспешных попыток перевода денег")
                .register(registry);
    }

    @Override
    public Mono<Void> cashOperation(OperationCash operationCash) {
        return accountRepository.findByLoginAndCurrency(operationCash.getLogin(), operationCash.getCurrency())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Аккаунт %s %s не найден"
                                .formatted(operationCash.getLogin(), operationCash.getCurrency()))))
                        .flatMap(accountEntity -> {
                            BigDecimal newBalance = operationCash.getAction() == CashAction.GET
                                    ? accountEntity.getBalance().subtract(operationCash.getAmount())
                                    : accountEntity.getBalance().add(operationCash.getAmount());
                            if (newBalance.signum() < 0) {
                                return notificationService.saveNotification(operationCash.getLogin(),
                                                "Недостаточно денег на счету")
                                        .then(Mono.error(new IllegalStateException("Недостаточно денег на счету")));
                            }
                            return accountRepository.save(accountEntity.setBalance(newBalance));
                        })
                .doOnError(IllegalStateException.class, throwable -> cashOperationFailedAmount.increment())
                .as(transactionalOperator::transactional)
                .then(notificationService.saveNotification(operationCash.getLogin(),
                        operationCash.getAction() == CashAction.GET
                                ? "Вы сняли деньги со счёта в размере %s %s".formatted(operationCash.getAmount(), operationCash.getCurrency())
                                : "Вы положили деньги на счёт в размере %s %s".formatted(operationCash.getAmount(), operationCash.getCurrency())));
    }

    @Override
    public Mono<Void> transferOperation(OperationTransfer operationTransfer) {
        boolean isSourcePrecedes = operationTransfer.getSourceLogin().compareTo(operationTransfer.getTargetLogin()) < 0;
        String firstLogin = isSourcePrecedes ?  operationTransfer.getSourceLogin() : operationTransfer.getTargetLogin();
        CurrencyCode firstCurrency = isSourcePrecedes ? operationTransfer.getSourceCurrency() : operationTransfer.getTargetCurrency();
        String secondLogin = isSourcePrecedes ?  operationTransfer.getTargetLogin() : operationTransfer.getSourceLogin();
        CurrencyCode secondCurrency = isSourcePrecedes ? operationTransfer.getTargetCurrency() : operationTransfer.getSourceCurrency();

        Mono<Void> tx = accountRepository.findByLoginAndCurrency(firstLogin, firstCurrency)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Аккаунт %s %s не найден".formatted(firstLogin, firstCurrency.toString()))))
                .flatMap(first -> accountRepository.findByLoginAndCurrency(secondLogin, secondCurrency)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Аккаунт %s %s не найден".formatted(secondLogin, secondCurrency.toString()))))
                                .flatMap(second -> {
                                    var source = isSourcePrecedes ? first : second;
                                    var target = isSourcePrecedes ? second : first;

                                    var newSource = source.getBalance().subtract(operationTransfer.getSourceAmount());
                                    if (newSource.signum() < 0) {
                                        return notificationService.saveNotification(operationTransfer.getSourceLogin(),
                                                        "Недостаточно денег на счету")
                                                .then(Mono.error(new IllegalStateException("Недостаточно денег на счету")));
                                    }

                                    source.setBalance(newSource);
                                    target.setBalance(target.getBalance().add(operationTransfer.getTargetAmount()));

                                    return accountRepository.saveAll(List.of(source, target))
                                            .then();
                                })
                )
                .as(transactionalOperator::transactional);

        Mono<Void> n1 = notificationService.saveNotification(operationTransfer.getSourceLogin(), "Вы отправили на счёт %s сумму %s %s"
                .formatted(operationTransfer.getTargetLogin(), operationTransfer.getSourceAmount(), operationTransfer.getSourceCurrency()));

        Mono<Void> n2 = notificationService.saveNotification(operationTransfer.getTargetLogin(), "Вы получили на счёт от %s сумму %s %s"
                .formatted(operationTransfer.getSourceLogin(), operationTransfer.getTargetAmount(), operationTransfer.getTargetCurrency()));

        return tx
                .doOnError(IllegalStateException.class, throwable -> transferOperationFailedAmount.increment())
                .then(Mono.when(n1, n2));
    }

    @Override
    public Flux<AccountInfo> getAccounts(String login) {
        return accountRepository.findAllByLogin(login)
                .map(accountMapper::toBO);
    }
}
