package by.tyv.account.model.bo;

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
public class OperationTransfer {
    private String sourceLogin;
    private String targetLogin;
    private BigDecimal targetAmount;
    private BigDecimal sourceAmount;
    private CurrencyCode sourceCurrency;
    private CurrencyCode targetCurrency;
}
