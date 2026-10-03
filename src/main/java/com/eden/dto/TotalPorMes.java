package com.eden.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record TotalPorMes(YearMonth mes, BigDecimal ingresos, BigDecimal gastos) {
}
