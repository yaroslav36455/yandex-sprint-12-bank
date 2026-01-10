package by.tyv.exchange.model.dto;

import by.tyv.exchange.enums.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(
        String title,
        CurrencyCode name,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "#0.00")
        BigDecimal value) { }
