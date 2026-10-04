package com.eden.dto;

import java.math.BigDecimal;

/**
 * Saldo que muestra el tablero y que se devuelve tras cada registro rapido
 * para actualizar la pantalla sin recargarla. El total es lo que hay por
 * repartir mas lo ahorrado en las cajitas activas.
 */
public record ResumenSaldo(BigDecimal disponible, BigDecimal enCajitas, ResumenPeriodo mes) {

    public BigDecimal total() {
        return disponible.add(enCajitas);
    }
}
