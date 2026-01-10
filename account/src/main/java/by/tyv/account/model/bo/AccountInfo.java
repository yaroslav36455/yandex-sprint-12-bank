package by.tyv.account.model.bo;

import by.tyv.account.enums.CurrencyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class AccountInfo {
    private Long id;
    private LocalDateTime createdAt;
    private Long userId;
    private BigDecimal balance;
    private CurrencyCode currency;
}
