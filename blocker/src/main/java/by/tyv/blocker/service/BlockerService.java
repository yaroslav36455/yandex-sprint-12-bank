package by.tyv.blocker.service;

import by.tyv.blocker.model.dto.OperationCashRequestDto;
import by.tyv.blocker.model.dto.OperationTransferRequestDto;
import reactor.core.publisher.Mono;

public interface BlockerService {
    Mono<Boolean> isAvailable(OperationCashRequestDto operationCashRequestDto);
    Mono<Boolean> isAvailable(OperationTransferRequestDto operationTransferRequestDto);
}
