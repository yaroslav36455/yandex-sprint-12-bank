package by.tyv.account.controller;

import by.tyv.account.mapper.AccountMapper;
import by.tyv.account.model.dto.AccountInfoDto;
import by.tyv.account.model.dto.OperationCashRequestDto;
import by.tyv.account.model.dto.TransferRequestDto;
import by.tyv.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping("/account/{login}/operation/cash")
    public Mono<Void> cashOperation(@PathVariable("login") String login,
                                    @RequestBody OperationCashRequestDto cashRequestDto) {
        return accountService.cashOperation(accountMapper.toBO(cashRequestDto).setLogin(login));
    }

    @PostMapping("/account/{login}/operation/transfer")
    public Mono<Void> transferOperation(@PathVariable("login") String login,
                                        @RequestBody TransferRequestDto transferRequestDto) {
        return accountService.transferOperation(accountMapper.toBO(transferRequestDto).setSourceLogin(login));
    }

    @GetMapping("/account/{login}")
    public Flux<AccountInfoDto> getAccounts(@PathVariable("login") String login) {
        return accountService.getAccounts(login)
                .map(accountMapper::toDto);
    }
}
