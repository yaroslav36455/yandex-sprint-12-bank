package by.tyv.frontui.mapper;

import by.tyv.frontui.enums.CurrencyCode;
import by.tyv.frontui.model.bo.Account;
import by.tyv.frontui.model.dto.AccountInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "value", source = "balance")
    @Mapping(target = "exists", constant = "true")
    Account toBO(AccountInfoDto accountInfoDto);

    List<CurrencyCode> toBoAccountList(List<String> accounts);
}
