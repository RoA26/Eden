package com.eden.dto;

import com.eden.modelo.TipoCategoria;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Presupuesto de una categoria contra lo gastado en el mes. */
public record EstadoPresupuesto(Long idPresupuesto, Long idCategoria, String categoria, TipoCategoria tipoCategoria,
                                BigDecimal presupuestado, BigDecimal gastado) {

    public static final int UMBRAL_ALERTA = 80;

    public int porcentaje() {
        return gastado.multiply(BigDecimal.valueOf(100)).divide(presupuestado, 0, RoundingMode.DOWN).intValue();
    }

    /** Porcentaje acotado a 100 para dibujar la barra. */
    public int porcentajeBarra() {
        return Math.min(porcentaje(), 100);
    }

    public BigDecimal restante() {
        return presupuestado.subtract(gastado);
    }

    public boolean excedido() {
        return gastado.compareTo(presupuestado) > 0;
    }

    public boolean enAlerta() {
        return !excedido() && porcentaje() >= UMBRAL_ALERTA;
    }
}
