package com.eden.dto;

import java.math.BigDecimal;

public record ResumenPeriodo(BigDecimal ingresos, BigDecimal gastosPersonales, BigDecimal costosOperativos) {

    public BigDecimal gastosTotales() {
        return gastosPersonales.add(costosOperativos);
    }

    public BigDecimal balance() {
        return ingresos.subtract(gastosTotales());
    }

    public boolean balancePositivo() {
        return balance().signum() >= 0;
    }
}
