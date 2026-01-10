package by.tyv.frontui.model.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class User {
    private String name;
    private String login;
    private LocalDate birthDate;
}
