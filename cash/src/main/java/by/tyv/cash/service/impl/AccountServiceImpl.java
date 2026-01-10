package by.tyv.cash.service.impl;

import by.tyv.cash.exception.ServiceException;
import by.tyv.cash.model.dto.ErrorResponseDto;
import by.tyv.cash.model.dto.OperationCashRequestDto;
import by.tyv.cash.service.AccountService;
import by.tyv.cash.service.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final WebClient webClient;
    private final TokenProvider tokenProvider;

    public AccountServiceImpl(@Value("${clients.account-service.url}") String accountServiceUrl,
                              WebClient.Builder webClientBuilder,
                              TokenProvider tokenProvider) {
        this.webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> doOperation(String login, OperationCashRequestDto operationCashRequestDto) {
        return tokenProvider.getCurrent().flatMap(token -> webClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("account", login, "operation", "cash").build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .bodyValue(operationCashRequestDto)
                .retrieve()
                .onStatus(status -> Objects.equals(status, HttpStatus.BAD_REQUEST),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка операции списания", throwable)));
    }
}
