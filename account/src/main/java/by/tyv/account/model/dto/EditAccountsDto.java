package by.tyv.account.model.dto;

import by.tyv.account.enums.CurrencyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class EditAccountsDto {
    private String name;
    private LocalDate birthDate;
    private List<CurrencyCode> accounts;
}
