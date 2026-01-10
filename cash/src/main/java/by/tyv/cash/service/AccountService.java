package by.tyv.cash.service;

import by.tyv.cash.model.dto.OperationCashRequestDto;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<Void> doOperation(String login, OperationCashRequestDto operationCashRequestDto);
}
