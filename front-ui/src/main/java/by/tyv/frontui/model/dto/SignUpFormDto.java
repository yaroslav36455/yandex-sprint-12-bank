package by.tyv.frontui.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class SignUpFormDto {
    private String login;
    private String password;
    private String confirmPassword;
    private String name;
    private LocalDate birthDate;
}
