package by.tyv.frontui.model.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class SignUpForm {
    private String login;
    private String password;
    private String confirmPassword;
    private String name;
    private LocalDate birthDate;
}
