package by.tyv.cash.service;

import by.tyv.cash.model.dto.OperationCashRequestDto;
import reactor.core.publisher.Mono;

public interface BlockerService {
    Mono<Boolean> isAvailableOperation(String login, OperationCashRequestDto operationCashRequestDto);
}
