package by.tyv.exchange.model.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Table(name = "exchange_rate")
public class ExchangeRateEntity {
    @Id
    private Long id;
    private String code;
    private BigDecimal rate;
}
