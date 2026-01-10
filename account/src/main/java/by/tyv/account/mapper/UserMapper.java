package by.tyv.account.mapper;

import by.tyv.account.model.bo.SignUpForm;
import by.tyv.account.model.bo.UserInfo;
import by.tyv.account.model.dto.SignUpFormDto;
import by.tyv.account.model.dto.UserInfoDto;
import by.tyv.account.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserInfo toBO(UserEntity entity);
    UserInfoDto toDTO(UserInfo bo);
    SignUpForm toBO(SignUpFormDto formDto);
}
