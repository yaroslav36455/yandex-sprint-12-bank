package by.tyv.cash.mapper;

import by.tyv.cash.model.dto.CashRequestDto;
import by.tyv.cash.model.dto.OperationCashRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CashMapper {

    OperationCashRequestDto mapToOperationRequestDto(CashRequestDto cashRequestDto);
}
