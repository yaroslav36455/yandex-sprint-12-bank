package by.tyv.exchangegenerator.model.dto;

import by.tyv.exchangegenerator.enums.CurrencyCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
public record ExchangeRateDto(CurrencyCode code, BigDecimal rate) { }