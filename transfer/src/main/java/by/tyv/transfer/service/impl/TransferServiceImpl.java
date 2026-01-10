package by.tyv.transfer.service.impl;

import by.tyv.transfer.mapper.TransferMapper;
import by.tyv.transfer.model.bo.Transfer;
import by.tyv.transfer.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountService accountService;
    private final BlockerService blockerService;
    private final NotificationService notificationService;
    private final ExchangeService exchangeService;
    private final TransferMapper transferMapper;

    @Override
    public Mono<Void> transferMoney(Transfer transfer) {
        return blockerService.isAvailableOperation(transfer.getSourceLogin(), transferMapper.mapToBlockerRequestDto(transfer))
                .switchIfEmpty(Mono.just(false))
                .flatMap(isAvailable -> isAvailable
                        ? exchangeService.convertMoney(transfer)
                        .flatMap(money -> accountService.doOperation(transferMapper.mapToAccountTransferBO(transfer).setTargetAmount(money))
                                .then(notificationService.saveNotification(transfer.getSourceLogin(), "Операция выполнена успешно")))
                        : notificationService.saveNotification(transfer.getSourceLogin(), "Операция запрещена"))
                .doOnError(ex -> log.error("Ошибка операции перевода денег", ex))
                .onErrorResume(ex -> notificationService
                        .saveNotification(transfer.getSourceLogin(), "Ошибка операции: " + ex.getMessage())
                        .then(Mono.error(ex)))
                .then();
    }
}
