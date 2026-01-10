package by.tyv.account.repository;

import by.tyv.account.enums.CurrencyCode;
import by.tyv.account.model.entity.AccountEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, Long> {

    @Query("""
              select a.id, a.created_at, a.user_id, a.balance, a.currency
              from account a join users u on u.id=a.user_id
              where u.login=:sourceLogin and a.currency=:currencyCode""")
    Mono<AccountEntity> findByLoginAndCurrency(@Param("sourceLogin") String sourceLogin,
                                               @Param("currencyCode") CurrencyCode currencyCode);

    @Query("""
              select a.id, a.created_at, a.user_id, a.balance, a.currency
              from account a join users u on u.id=a.user_id
              where u.login=:login""")
    Flux<AccountEntity> findAllByLogin(@Param("login") String login);
}
