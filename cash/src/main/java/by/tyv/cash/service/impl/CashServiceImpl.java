package by.tyv.cash.service.impl;

import by.tyv.cash.mapper.CashMapper;
import by.tyv.cash.model.dto.CashRequestDto;
import by.tyv.cash.model.dto.OperationCashRequestDto;
import by.tyv.cash.service.AccountService;
import by.tyv.cash.service.BlockerService;
import by.tyv.cash.service.CashService;
import by.tyv.cash.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {
    private final BlockerService blockerService;
    private final AccountService accountService;
    private final NotificationService notificationService;
    private final CashMapper cashMapper;

    @Override
    public Mono<Void> cashOperation(String login, CashRequestDto cashRequestDto) {
        OperationCashRequestDto operationCashRequestDto = cashMapper.mapToOperationRequestDto(cashRequestDto);
        return blockerService.isAvailableOperation(login, operationCashRequestDto)
                .switchIfEmpty(Mono.just(false))
                .flatMap(isAvailable -> isAvailable
                        ? accountService.doOperation(login, operationCashRequestDto)
                            .then(notificationService.saveNotification(login, "Операция выполнена успешно"))
                        : notificationService.saveNotification(login, "Операция запрещена"))
                .doOnError(ex -> log.error("Ошибка операции списания/зачисления", ex))
                .onErrorResume(ex -> notificationService
                        .saveNotification(login, "Ошибка операции: " + ex.getMessage())
                        .then(Mono.error(ex)))
                .then();
    }
}
