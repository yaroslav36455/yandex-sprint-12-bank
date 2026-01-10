package by.tyv.account.service.impl;

import by.tyv.account.enums.CashAction;
import by.tyv.account.mapper.AccountMapper;
import by.tyv.account.model.bo.AccountInfo;
import by.tyv.account.model.bo.OperationCash;
import by.tyv.account.model.bo.OperationTransfer;
import by.tyv.account.model.entity.AccountEntity;
import by.tyv.account.repository.AccountRepository;
import by.tyv.account.service.AccountService;
import by.tyv.account.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionalOperator transactionalOperator;
    private final NotificationService notificationService;
    private final AccountMapper accountMapper;

    @Override
    public Mono<Void> cashOperation(OperationCash operationCash) {
        return accountRepository.findByLoginAndCurrency(operationCash.getLogin(), operationCash.getCurrency())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Аккаунт не найден")))
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
                .as(transactionalOperator::transactional)
                .then(notificationService.saveNotification(operationCash.getLogin(),
                        operationCash.getAction() == CashAction.GET
                                ? "Вы сняли деньги со счёта в размере %s %s".formatted(operationCash.getAmount(), operationCash.getCurrency())
                                : "Вы положили деньги на счёт в размере %s %s".formatted(operationCash.getAmount(), operationCash.getCurrency())));
    }

    @Override
    public Mono<Void> transferOperation(OperationTransfer operationTransfer) {
        Flux<AccountEntity> tx =  Mono.zip(
                        accountRepository.findByLoginAndCurrency(operationTransfer.getSourceLogin(), operationTransfer.getSourceCurrency()),
                        accountRepository.findByLoginAndCurrency(operationTransfer.getTargetLogin(), operationTransfer.getTargetCurrency()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Аккаунт не найден")))
                .flatMapMany(tuple -> {
                    var source = tuple.getT1();
                    var target = tuple.getT2();

                    var newSource = source.getBalance().subtract(operationTransfer.getSourceAmount());
                    if (newSource.signum() < 0) {
                        return notificationService.saveNotification(operationTransfer.getSourceLogin(),
                                        "Недостаточно денег на счету")
                                .then(Mono.error(new IllegalStateException("Недостаточно денег на счету")));
                    }

                    source.setBalance(newSource);
                    target.setBalance(target.getBalance().add(operationTransfer.getTargetAmount()));

                    return accountRepository.saveAll(List.of(source, target));
                })
                .as(transactionalOperator::transactional);

        Mono<Void> n1 = notificationService.saveNotification(operationTransfer.getSourceLogin(), "Вы отправили на счёт %s сумму %s %s"
                .formatted(operationTransfer.getTargetLogin(), operationTransfer.getSourceAmount(), operationTransfer.getSourceCurrency()));

        Mono<Void> n2 = notificationService.saveNotification(operationTransfer.getTargetLogin(), "Вы получили на счёт от %s сумму %s %s"
                .formatted(operationTransfer.getSourceLogin(), operationTransfer.getTargetAmount(), operationTransfer.getTargetCurrency()));

        return tx.then(Mono.when(n1, n2));
    }

    @Override
    public Flux<AccountInfo> getAccounts(String login) {
        return accountRepository.findAllByLogin(login)
                .map(accountMapper::toBO);
    }
}
