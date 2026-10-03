package com.eden.dto;

import com.eden.modelo.CalculadoraReparto;

import java.math.BigDecimal;

public record PropuestaReparto(BigDecimal disponible, CalculadoraReparto.Propuesta propuesta,
                               BigDecimal sumaPorcentajes, boolean hayResto) {

    public boolean porcentajesIncompletos() {
        return sumaPorcentajes.compareTo(BigDecimal.valueOf(100)) < 0;
    }
}
