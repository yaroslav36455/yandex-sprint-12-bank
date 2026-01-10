package by.tyv.transfer.service;

import by.tyv.transfer.model.dto.AccountTransfer;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<Void> doOperation(AccountTransfer accountTransfer);
}
