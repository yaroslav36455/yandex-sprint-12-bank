package by.tyv.cash.model.dto;

import by.tyv.cash.enums.Action;
import by.tyv.cash.enums.CurrencyCode;
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
    private Action action;
    private BigDecimal amount;
}
