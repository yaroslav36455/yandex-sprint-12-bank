package by.tyv.frontui.model.dto;

import by.tyv.frontui.enums.CurrencyCode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class AccountsUpdateDto {
    private String name;
    private LocalDate birthDate;
    private List<CurrencyCode> accounts;
}
