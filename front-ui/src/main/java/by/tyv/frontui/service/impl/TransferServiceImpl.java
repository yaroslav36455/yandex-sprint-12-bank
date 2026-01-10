package by.tyv.frontui.service.impl;

import by.tyv.frontui.exception.ServiceException;
import by.tyv.frontui.model.dto.ErrorResponseDto;
import by.tyv.frontui.model.dto.TransferRequestDto;
import by.tyv.frontui.service.TokenProvider;
import by.tyv.frontui.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final WebClient transferWebClient;
    private final TokenProvider tokenProvider;

    public TransferServiceImpl(@Value("${clients.transfer-service.url}") String transferServiceUrl,
                               WebClient.Builder webClientBuilder,
                               TokenProvider tokenProvider) {
        this.transferWebClient = webClientBuilder.baseUrl(transferServiceUrl).build();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> makeTransfer(String login, TransferRequestDto transferRequestDto) {
        return tokenProvider.getNewTechnical().flatMap(token -> transferWebClient.post()
                .uri(UriComponentsBuilder.fromPath("/transfer/{login}").buildAndExpand(login).toString())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(transferRequestDto)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка выполнения операции перевода денег", throwable)));
    }
}
