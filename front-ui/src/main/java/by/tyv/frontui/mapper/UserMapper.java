package by.tyv.frontui.mapper;

import by.tyv.frontui.model.bo.SignUpForm;
import by.tyv.frontui.model.bo.User;
import by.tyv.frontui.model.dto.SignUpFormDto;
import by.tyv.frontui.model.dto.UserInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toBO(UserInfoDto dto);
    SignUpForm toBO(SignUpFormDto dto);
}
