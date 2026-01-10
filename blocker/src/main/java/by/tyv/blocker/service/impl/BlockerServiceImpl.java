package by.tyv.blocker.service.impl;

import by.tyv.blocker.model.dto.OperationCashRequestDto;
import by.tyv.blocker.model.dto.OperationTransferRequestDto;
import by.tyv.blocker.service.BlockerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.random.RandomGenerator;

@Service
@Slf4j
public class BlockerServiceImpl implements BlockerService {
    static private final int BLOCK_PERCENT = 10;
    private final RandomGenerator randomGenerator = new Random();

    @Override
    public Mono<Boolean> isAvailable(OperationCashRequestDto operationCashRequestDto) {
        return Mono.just(this.isAvailable());
    }

    @Override
    public Mono<Boolean> isAvailable(OperationTransferRequestDto operationTransferRequestDto) {
        return Mono.just(this.isAvailable());
    }

    private boolean isAvailable() {
        return randomGenerator.nextInt(0, 100) >= BLOCK_PERCENT;
    }
}
