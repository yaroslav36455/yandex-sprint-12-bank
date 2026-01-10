package by.tyv.frontui.model.bo;

import by.tyv.frontui.enums.CurrencyCode;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private BigDecimal value;
    private CurrencyCode currency;
    private boolean exists;
}
