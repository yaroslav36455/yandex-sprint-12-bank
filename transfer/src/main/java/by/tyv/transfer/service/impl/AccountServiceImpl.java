package by.tyv.transfer.service.impl;

import by.tyv.transfer.exception.ServiceException;
import by.tyv.transfer.mapper.TransferMapper;
import by.tyv.transfer.model.dto.AccountTransfer;
import by.tyv.transfer.model.dto.ErrorResponseDto;
import by.tyv.transfer.service.AccountService;
import by.tyv.transfer.service.TokenProvider;
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
    private final TransferMapper transferMapper;
    private final TokenProvider tokenProvider;

    public AccountServiceImpl(@Value("${clients.account-service.url}") String accountServiceUrl,
                              WebClient.Builder webClientBuilder,
                              TransferMapper transferMapper,
                              TokenProvider tokenProvider) {
        this.webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
        this.transferMapper = transferMapper;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> doOperation(AccountTransfer accountTransfer) {
        return tokenProvider.getCurrent()
                .flatMap(token -> webClient.post()
                        .uri(uriBuilder -> uriBuilder.pathSegment("account", accountTransfer.getSourceLogin(), "operation", "transfer").build())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .bodyValue(transferMapper.mapToAccountRequestDto(accountTransfer))
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
