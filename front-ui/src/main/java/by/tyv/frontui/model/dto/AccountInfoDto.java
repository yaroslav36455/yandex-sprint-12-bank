package by.tyv.frontui.model.dto;

import by.tyv.frontui.enums.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class AccountInfoDto {
    private Long id;
    private LocalDateTime createdAt;
    private Long userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#0.00")
    private BigDecimal balance;
    private CurrencyCode currency;
}
