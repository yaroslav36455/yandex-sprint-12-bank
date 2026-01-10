package by.tyv.frontui.service.impl;

import by.tyv.frontui.exception.ServiceException;
import by.tyv.frontui.model.dto.ErrorResponseDto;
import by.tyv.frontui.model.dto.OperationCashRequestDto;
import by.tyv.frontui.service.CashService;
import by.tyv.frontui.service.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CashServiceImpl implements CashService {

    private final WebClient cashWebClient;
    private final TokenProvider tokenProvider;

    public CashServiceImpl(@Value("${clients.cash-service.url}") String cashServiceUrl,
                           WebClient.Builder webClientBuilder,
                           TokenProvider tokenProvider) {
        this.cashWebClient = webClientBuilder.baseUrl(cashServiceUrl).build();
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> cashOperation(String login, OperationCashRequestDto cashRequestDto) {
        return tokenProvider.getNewTechnical().flatMap(token -> cashWebClient.post()
                .uri(UriComponentsBuilder.fromPath("/cash/{login}").buildAndExpand(login).toString())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cashRequestDto)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        resp -> resp.bodyToMono(ErrorResponseDto.class)
                                .defaultIfEmpty(new ErrorResponseDto("Неизвестная ошибка"))
                                .flatMap(err -> Mono.error(new ServiceException(err.getErrorMessage()))))
                .toBodilessEntity()
                .then()
                .doOnError(throwable -> log.error("Ошибка выполнения операции с наличной валютой", throwable)));
    }
}
