package by.tyv.transfer.service;

import by.tyv.transfer.model.dto.BlockerCheckRequestDto;
import reactor.core.publisher.Mono;

public interface BlockerService {
    Mono<Boolean> isAvailableOperation(String login, BlockerCheckRequestDto operationtransferRequestDto);
}
