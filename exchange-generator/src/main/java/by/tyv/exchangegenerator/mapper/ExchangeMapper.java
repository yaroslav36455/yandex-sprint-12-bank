package by.tyv.exchangegenerator.mapper;

import by.tyv.exchangegenerator.model.bo.ExchangeRate;
import by.tyv.exchangegenerator.model.dto.ExchangeRateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeMapper {
    List<ExchangeRateDto> mapToDto(List<ExchangeRate> exchangeRates);
    ExchangeRateDto mapToDto(ExchangeRate exchangeRate);
}
