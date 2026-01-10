package by.tyv.frontui.model.dto;

import by.tyv.frontui.enums.CurrencyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class TransferRequestDto {
    private String targetLogin;
    private CurrencyCode sourceCurrency;
    private CurrencyCode targetCurrency;
    private BigDecimal sourceAmount;
}
