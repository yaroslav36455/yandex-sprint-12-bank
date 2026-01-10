package by.tyv.exchange.model.dto;

import by.tyv.exchange.enums.CurrencyCode;

import java.math.BigDecimal;

public record ExchangeRateRequestDto(CurrencyCode code, BigDecimal rate) { }
