package by.tyv.transfer.model.bo;

import by.tyv.transfer.enums.CurrencyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class Exchange {
    BigDecimal rate;
    CurrencyCode currency;
}
