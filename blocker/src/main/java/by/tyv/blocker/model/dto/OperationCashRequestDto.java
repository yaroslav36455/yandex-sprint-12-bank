package by.tyv.blocker.model.dto;

import by.tyv.blocker.enums.CashAction;
import by.tyv.blocker.enums.CurrencyCode;
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
