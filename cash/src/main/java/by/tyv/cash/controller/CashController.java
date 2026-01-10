package by.tyv.cash.controller;

import by.tyv.cash.model.dto.CashRequestDto;
import by.tyv.cash.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class CashController {
    private final CashService cashService;

    @PostMapping("/cash/{login}")
    public Mono<Void> postCash(@PathVariable("login") String login,
                               @RequestBody CashRequestDto cashRequestDto) {
        return cashService.cashOperation(login, cashRequestDto);
    }
}
