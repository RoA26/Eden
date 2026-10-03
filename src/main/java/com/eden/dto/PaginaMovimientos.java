package com.eden.dto;

import com.eden.modelo.Movimiento;

import java.math.BigDecimal;
import java.util.List;

public record PaginaMovimientos(List<Movimiento> movimientos, int pagina, int tamano, long totalRegistros,
                                BigDecimal totalIngresos, BigDecimal totalGastos) {

    public boolean hayAnterior() {
        return pagina > 0;
    }

    public boolean haySiguiente() {
        return (long) (pagina + 1) * tamano < totalRegistros;
    }

    public BigDecimal balance() {
        return totalIngresos.subtract(totalGastos);
    }
}
