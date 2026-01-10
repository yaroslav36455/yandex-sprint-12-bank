package by.tyv.account.controller;

import by.tyv.account.mapper.AccountMapper;
import by.tyv.account.mapper.UserMapper;
import by.tyv.account.model.dto.EditAccountsDto;
import by.tyv.account.model.dto.PasswordUpdateDto;
import by.tyv.account.model.dto.SignUpFormDto;
import by.tyv.account.model.dto.UserInfoDto;
import by.tyv.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    @GetMapping("/user")
    public Flux<UserInfoDto> getUsers() {
        return userService.getUsers()
                .map(userMapper::toDTO);
    }

    @PostMapping("/user/{login}/editPassword")
    public Mono<Void> postEditPassword(@PathVariable("login") String login,
                                       @RequestBody PasswordUpdateDto passwordUpdate) {
        return userService.updatePassword(login, passwordUpdate.getPassword(), passwordUpdate.getConfirmPassword());
    }

    @PostMapping("/user/{login}/editUserAccounts")
    public Mono<Void> postEditAccount(@PathVariable("login") String login,
                                      @RequestBody EditAccountsDto editAccountsDto) {
        return userService.updateAccounts(login, accountMapper.toBO(editAccountsDto));
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> postSignup(@RequestBody SignUpFormDto form) {
        return userService.signUp(userMapper.toBO(form));
    }
}
