package by.tyv.account.model.bo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class UserInfo {
    private Long id;
    private LocalDateTime createdAt;
    private String login;
    private String name;
    private LocalDate birthDate;
}
