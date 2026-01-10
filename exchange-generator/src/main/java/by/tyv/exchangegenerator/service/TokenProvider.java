package by.tyv.exchangegenerator.service;

import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> getNewTechnical();
}
