package by.tyv.transfer.model.dto;

import by.tyv.transfer.enums.CurrencyCode;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(String title, BigDecimal value, CurrencyCode name) { }
