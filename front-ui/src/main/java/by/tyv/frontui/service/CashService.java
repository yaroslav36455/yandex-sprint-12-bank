package by.tyv.frontui.service;

import by.tyv.frontui.model.dto.OperationCashRequestDto;
import reactor.core.publisher.Mono;

public interface CashService {
    Mono<Void> cashOperation(String login, OperationCashRequestDto cashRequestDto);
}
