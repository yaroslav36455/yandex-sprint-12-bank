package by.tyv.frontui.service;

import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> getCurrent();
    Mono<String> getNewTechnical();
    Mono<String> getCurrentUsername();
}
