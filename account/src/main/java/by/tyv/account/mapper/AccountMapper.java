package by.tyv.account.mapper;

import by.tyv.account.model.bo.AccountInfo;
import by.tyv.account.model.bo.EditAccounts;
import by.tyv.account.model.bo.OperationCash;
import by.tyv.account.model.bo.OperationTransfer;
import by.tyv.account.model.dto.AccountInfoDto;
import by.tyv.account.model.dto.EditAccountsDto;
import by.tyv.account.model.dto.OperationCashRequestDto;
import by.tyv.account.model.dto.TransferRequestDto;
import by.tyv.account.model.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "login", ignore = true)
    OperationCash toBO(OperationCashRequestDto requestDto);

    @Mapping(target = "sourceLogin", ignore = true)
    OperationTransfer toBO(TransferRequestDto transferRequestDto);

    AccountInfo toBO(AccountEntity entity);

    AccountInfoDto toDto(AccountInfo bo);

    EditAccounts toBO(EditAccountsDto editAccountsDto);
}
