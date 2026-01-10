package by.tyv.exchangegenerator.model.bo;

import by.tyv.exchangegenerator.enums.CurrencyCode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExchangeRate {
    CurrencyCode code;
    BigDecimal rate;
}
