package com.eden.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param sinProducido true si habia una fuente diaria activa y ese dia
 *                     (ya pasado) no se registro producido.
 */
public record DiaCalendario(LocalDate fecha, boolean delMes, boolean hoy,
                            BigDecimal ingresos, BigDecimal gastos,
                            boolean conProducido, boolean sinProducido) {

    public boolean tieneIngresos() {
        return ingresos.signum() > 0;
    }

    public boolean tieneGastos() {
        return gastos.signum() > 0;
    }
}
