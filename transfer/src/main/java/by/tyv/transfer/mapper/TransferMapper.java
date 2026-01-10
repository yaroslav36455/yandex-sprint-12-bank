package by.tyv.transfer.mapper;

import by.tyv.transfer.model.bo.Transfer;
import by.tyv.transfer.model.dto.AccountTransfer;
import by.tyv.transfer.model.dto.AccountTransferRequestDto;
import by.tyv.transfer.model.dto.BlockerCheckRequestDto;
import by.tyv.transfer.model.dto.TransferRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransferMapper {

    @Mapping(ignore = true, target = "sourceLogin")
    Transfer mapToTransferBO(TransferRequestDto operationTransfer);
    BlockerCheckRequestDto mapToBlockerRequestDto(Transfer transfer);

    @Mapping(ignore = true, target = "targetAmount")
    AccountTransfer mapToAccountTransferBO(Transfer transfer);

    AccountTransferRequestDto mapToAccountRequestDto(AccountTransfer accountTransfer);
}
