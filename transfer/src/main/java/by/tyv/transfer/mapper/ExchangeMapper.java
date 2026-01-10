package by.tyv.transfer.mapper;

import by.tyv.transfer.model.bo.Exchange;
import by.tyv.transfer.model.dto.ExchangeRateResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeMapper {

    @Mapping(target = "currency", source = "name")
    @Mapping(target = "rate", source = "value")
    Exchange toExchangeBO(ExchangeRateResponseDto exchangeRateResponseDto);
}
