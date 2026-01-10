package by.tyv.transfer.service;

import by.tyv.transfer.model.bo.Transfer;
import reactor.core.publisher.Mono;

public interface TransferService {
    Mono<Void> transferMoney(Transfer transfer);
}
