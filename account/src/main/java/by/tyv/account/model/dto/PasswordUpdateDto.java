package by.tyv.account.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PasswordUpdateDto {
    private final String password;
    private final String confirmPassword;
}
