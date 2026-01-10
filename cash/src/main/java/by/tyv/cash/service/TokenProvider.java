package by.tyv.cash.service;

import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> getCurrent();
    Mono<String> getNewTechnical();
}
