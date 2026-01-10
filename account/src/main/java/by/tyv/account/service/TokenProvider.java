package by.tyv.account.service;

import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> getNewTechnical();
    Mono<String> getCurrent();

    Mono<String> getNewUserManagmentToken();
}
