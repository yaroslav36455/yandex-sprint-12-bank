package by.tyv.account.model.dto;

import by.tyv.account.enums.CashAction;
import by.tyv.account.enums.CurrencyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class OperationCashRequestDto {
    private CurrencyCode currency;
    private CashAction action;
    private BigDecimal amount;
}
