package com.eden.dto;

import com.eden.modelo.Cajita;
import com.eden.modelo.Movimiento;
import com.eden.modelo.ProgresoMeta;
import com.eden.modelo.Recurrente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record Tablero(LocalDate hoy,
                      String nombreMes,
                      BigDecimal disponible,
                      ResumenPeriodo mesActual,
                      ResumenPeriodo mesAnterior,
                      List<Cajita> cajitas,
                      BigDecimal totalCajitas,
                      List<ResumenFuente> fuentes,
                      List<Movimiento> ultimosMovimientos,
                      Map<String, Object> datosGraficas,
                      List<Recurrente> pendientes,
                      List<EstadoPresupuesto> alertasPresupuesto,
                      List<ProgresoMeta> metas) {

    /** Saldo total: lo que hay por repartir mas lo ahorrado en las cajitas. */
    public BigDecimal saldoTotal() {
        return disponible.add(totalCajitas);
    }

    public boolean sinMovimientos() {
        return ultimosMovimientos.isEmpty();
    }
}
