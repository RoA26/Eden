package com.eden.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Estado de resultados de una fuente en un periodo (RN-02). */
public record ResumenFuente(Long idFuente, String nombre, BigDecimal bruto, BigDecimal costos, int diasTrabajados) {

    public BigDecimal utilidad() {
        return bruto.subtract(costos);
    }

    public BigDecimal promedioDiario() {
        return diasTrabajados == 0 ? BigDecimal.ZERO
                : utilidad().divide(BigDecimal.valueOf(diasTrabajados), 0, RoundingMode.HALF_UP);
    }

    /** Margen de utilidad en porcentaje entero, o null si no hubo ingresos. */
    public Integer margen() {
        return bruto.signum() == 0 ? null
                : utilidad().multiply(BigDecimal.valueOf(100)).divide(bruto, 0, RoundingMode.HALF_UP).intValue();
    }
}
