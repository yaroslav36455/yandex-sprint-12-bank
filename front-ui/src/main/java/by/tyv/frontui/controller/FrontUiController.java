package by.tyv.frontui.controller;

import by.tyv.frontui.mapper.AccountMapper;
import by.tyv.frontui.mapper.UserMapper;
import by.tyv.frontui.model.dto.OperationCashRequestDto;
import by.tyv.frontui.model.dto.SignUpFormDto;
import by.tyv.frontui.model.dto.TransferRequestDto;
import by.tyv.frontui.service.AccountService;
import by.tyv.frontui.service.FrontUiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FrontUiController {
    private final FrontUiService frontUiService;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final AccountService accountService;

    // а) GET "/" - редирект на "/main"
    @GetMapping("/")
    public Mono<RedirectView> getRedirectMainPage() {
        return Mono.just(new RedirectView("/main"));
    }

    /*
	б) GET "/main" - главная страница
		Возвращает:
            шаблон "main.html"
            используется модель для заполнения шаблона:
                "login" - строка с логином пользователя
                "name" - строка с фамилией и именем пользователя
                "birthdate" - LocalDate с датой рождения пользователя
                "accounts" - список всех зарегистрированных пользователей:
                    "currency" - enum валюта:
                        "title" - название валюты
                        "name()" - код валюты
                    "value" - сумма на счету пользователя в этой валюте
                    "exists" - true, если у пользователя есть счет в этой валюте, false, если нет
                "currency" - список всех доступных валют:
                    "title" - название валюты
                    "name()" - код валюты
                "users" - список всех пользователей:
                    "login" - логин пользователя
                    "name" - фамилия и имя пользователя
                "passwordErrors" - список ошибок при смене пароля (null, если не выполнялась смена пароля)
                "userAccountsErrors" - список ошибок при редактировании настроек аккаунта (null, если не выполнялось редактирование)
                "cashErrors" - список ошибок при внесении/снятии денег (null, если не выполнялось внесение/снятие)
                "transferErrors" - список ошибок при переводе между своими счетами (null, если не выполнялся перевод)
                "transferOtherErrors" - список ошибок при переводе на счет другого пользователя (null, если не выполнялся перевод)
    */
    @GetMapping("/main")
    public Mono<Rendering> getMainPage() {
        return frontUiService.buildMainPage();
    }

    /*
    д) POST "/user/{login}/cash" - эндпоинт внесения/снятия денег (записывает список ошибок, если есть, в cashErrors)
        Параметры:
            login - логин пользователя
            currency - строка с валютой
            value - сумма внесения/снятия
            action - действие (enum PUT иди GET)
        Возвращает:
            редирект на "/main"
    */
    @PostMapping("/user/{login}/cash")
    public Mono<Rendering> postCash(@PathVariable("login") String login,
                                    @ModelAttribute OperationCashRequestDto cashRequestDto) {
        return frontUiService.cashOperation(login, cashRequestDto);
    }

    /*
    ж) GET "/signup" - страница регистрации нового пользователя
    Возвращает:
                шаблон "signup.html"
    */
    @GetMapping("/signup")
    public Mono<String> getSignupPage() {
        return Mono.just("signup");
    }

    /*
    з) POST "/signup" - эндпоинт регистрации нового пользователя
        Параметры:
            login - логин пользователя
            password - пароль пользователя
            confirm_password - пароль пользователя второй раз
            name - фамилия и имя пользователя
            birthdate - дата рождения пользователя (LocalDate)
            @RequestParam("login") String login,
    Возвращает:
                редирект на "/main"
            В случае ошибок возвращает:
                шаблон "signup.html"
                используется модель для заполнения шаблона:
                    "login" - строка с логином пользователя
                    "name" - строка с фамилией и именем пользователя
                    "birthdate" - LocalDate с датой рождения пользователя
                    "accounts" - список всех зарегистрированных пользователей
                    "errors" - список ошибок при регистрации
    */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Rendering> postSignup(@ModelAttribute("form") SignUpFormDto form) {
        return frontUiService.signUp(userMapper.toBO(form));
    }

    /*
    е) POST "/user/{login}/transfer" - эндпоинт перевода денег между своими счетами и перевода денег на счёт другого пользователя (один эндпоинт для того и другого, записывает список ошибок, если есть, в transferErrors или в transferOtherErrors)
        Параметры:
            login - логин пользователя.
            from_currency - строка с валютой счета, с которого переводятся деньги.
            to_currency - строка с валютой счета, на который переводятся деньги.
            value - сумма внесения/снятия.
            to_login - логин пользователя, которому переводятся деньги.
        Возвращает:
            редирект на "/main"
    */
    @PostMapping("/user/{login}/transfer")
    public Mono<Rendering> postTransferMoney(@PathVariable("login") String login,
                                             @ModelAttribute TransferRequestDto transferRequestDto) {
        return frontUiService.makeTransfer(login, transferRequestDto);
    }

    /*
    в) POST "/user/{login}/editPassword" - эндпоинт смены пароля (записывает список ошибок, если есть, в passwordErrors)
        Параметры:
            login - логин пользователя
            password - новый пароль
            confirm_password - новый пароль второй раз
        Возвращает:
            редирект на "/main"
    */
    @PostMapping("/user/{login}/editPassword")
    public Mono<Rendering> postEditPassword(@PathVariable("login") String login,
                                            ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(form -> {
                    String password = form.getFirst("password");
                    String confirmPassword = form.getFirst("confirmPassword");
                    return frontUiService.updatePassword(login, password, confirmPassword);
                });
    }

    /*
    г) POST "/user/{login}/editUserAccounts" - эндпоинт редактирования аккаунта (записывает список ошибок, если есть, в userAccountsErrors)
        Параметры:
            login - логин пользователя
            name - фамилия и имя пользователя
            birthdate - дата рождения пользователя (LocalDate)
            account - список строк с валютами пользователя, для которых у него есть счета
        Возвращает:
            редирект на "/main"
    */
    @PostMapping("/user/{login}/editUserAccounts")
    public Mono<Rendering> postEditAccount(@PathVariable("login") String login,
                                           ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(form -> {
                    String name = form.getFirst("name");
                    String birthDate = form.getFirst("birthDate");
                    List<String> accounts = form.get("account");
                    return frontUiService.updateAccounts(login,
                            StringUtils.hasText(name) ? name : null,
                            StringUtils.hasText(birthDate) ? LocalDate.parse(birthDate) : null,
                            accountMapper.toBoAccountList(accounts));
                });
    }
}
