package by.tyv.cash.model.dto;

import by.tyv.cash.enums.Action;
import by.tyv.cash.enums.CurrencyCode;

import java.math.BigDecimal;

public record CashRequestDto(CurrencyCode currency, BigDecimal amount, Action action) {
}