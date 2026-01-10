package by.tyv.frontui.service;

import by.tyv.frontui.model.dto.TransferRequestDto;
import reactor.core.publisher.Mono;

public interface TransferService {
    Mono<Void> makeTransfer(String login, TransferRequestDto transferRequestDto);
}
