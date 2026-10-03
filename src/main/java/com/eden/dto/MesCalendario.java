package com.eden.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MesCalendario(YearMonth mes, List<List<DiaCalendario>> semanas,
                            BigDecimal totalIngresos, BigDecimal totalGastos,
                            int diasConProducido, int diasSinProducido) {

    public YearMonth mesAnterior() {
        return mes.minusMonths(1);
    }

    public YearMonth mesSiguiente() {
        return mes.plusMonths(1);
    }
}
