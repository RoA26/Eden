package com.eden.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TotalPorDia(LocalDate fecha, BigDecimal ingresos, BigDecimal gastos, BigDecimal producido) {
}
