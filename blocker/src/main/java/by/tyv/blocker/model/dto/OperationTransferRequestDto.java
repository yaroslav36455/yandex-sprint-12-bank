package by.tyv.blocker.model.dto;

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
public class OperationTransferRequestDto {
    private CurrencyCode sourceCurrency;
    private CurrencyCode targetCurrency;
    private BigDecimal sourceAmount;
}
