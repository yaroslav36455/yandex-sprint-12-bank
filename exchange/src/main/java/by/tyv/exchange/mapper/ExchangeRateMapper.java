package by.tyv.exchange.mapper;

import by.tyv.exchange.enums.CurrencyCode;
import by.tyv.exchange.model.dto.ExchangeRateRequestDto;
import by.tyv.exchange.model.dto.ExchangeRateResponseDto;
import by.tyv.exchange.model.entity.ExchangeRateEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeRateMapper {
    @Mapping(target = "name", source = "code")
    @Mapping(target = "value", source = "rate", qualifiedByName = "SetScale")
    @Mapping(target = "title", source = "code", qualifiedByName = "CurrencyCodeEntityToResponseDto")
    ExchangeRateResponseDto toResponseDto(ExchangeRateEntity exchangeRate);

    List<ExchangeRateEntity> toEntity(List<ExchangeRateRequestDto> exchangeRates);
    @Mapping(target = "id", ignore = true)
    ExchangeRateEntity toEntity(ExchangeRateRequestDto exchangeRate);

    @Named("CurrencyCodeEntityToResponseDto")
    default String currencyCodeEntityToResponseDto(String currencyCodeStr) {
        return CurrencyCode.valueOf(currencyCodeStr).getTitle();
    }

    @Named("SetScale")
    default BigDecimal setScale(BigDecimal rate) {
        return rate.setScale(2, RoundingMode.HALF_UP);
    }
}
