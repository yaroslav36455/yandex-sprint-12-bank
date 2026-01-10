package by.tyv.cash.service;

import by.tyv.cash.model.dto.CashRequestDto;
import reactor.core.publisher.Mono;

public interface CashService {
    Mono<Void> cashOperation(String login, CashRequestDto cashRequestDto);
}
