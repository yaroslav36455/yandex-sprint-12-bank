package by.tyv.cash.controller;

import by.tyv.cash.exception.NotificationException;
import by.tyv.cash.model.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class Advices {

    @ExceptionHandler(value = {Throwable.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponseDto> handleExceptionBadRequest(Throwable ex) {
        return Mono.just(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(value = {NotificationException.class})
    @ResponseStatus(HttpStatus.OK)
    public Mono<ErrorResponseDto> handleExceptionNotification(NotificationException ex) {
        return Mono.just(new ErrorResponseDto(ex.getMessage()));
    }
}
