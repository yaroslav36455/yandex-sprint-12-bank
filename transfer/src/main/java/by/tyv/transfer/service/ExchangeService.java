package by.tyv.transfer.service;

import by.tyv.transfer.model.bo.Transfer;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ExchangeService {
    Mono<BigDecimal> convertMoney(Transfer transfer);
}
