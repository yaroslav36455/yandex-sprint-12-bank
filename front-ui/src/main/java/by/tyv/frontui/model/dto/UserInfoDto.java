package by.tyv.frontui.model.dto;

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
public class UserInfoDto {
    private Long id;
    private LocalDateTime createdAt;
    private String login;
    private String name;
    private LocalDate birthDate;
}
