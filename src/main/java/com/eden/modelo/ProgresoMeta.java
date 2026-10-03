package com.eden.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Progreso de una meta calculado a partir del saldo de su cajita.
 * Logica pura para poder probarla sin base de datos.
 *
 * @param mesesRestantes      meses completos o parciales hasta la fecha objetivo (null si no tiene fecha).
 * @param aporteMensual       cuanto aportar cada mes para llegar a tiempo (null si no tiene fecha o ya se cumplio).
 */
public record ProgresoMeta(Meta meta, int porcentaje, BigDecimal faltante, Integer mesesRestantes,
                           BigDecimal aporteMensual, boolean cumplida, boolean vencida) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public static ProgresoMeta calcular(Meta meta, LocalDate hoy) {
        BigDecimal saldo = meta.getSaldoCajita().max(BigDecimal.ZERO);
        BigDecimal objetivo = meta.getMontoObjetivo();
        boolean cumplida = saldo.compareTo(objetivo) >= 0;
        BigDecimal faltante = cumplida ? BigDecimal.ZERO : objetivo.subtract(saldo);
        int porcentaje = saldo.multiply(CIEN).divide(objetivo, 0, RoundingMode.DOWN).min(CIEN).intValue();

        Integer meses = null;
        BigDecimal aporte = null;
        boolean vencida = false;
        LocalDate fecha = meta.getFechaObjetivo();
        if (fecha != null) {
            vencida = !cumplida && fecha.isBefore(hoy);
            long mesesCompletos = ChronoUnit.MONTHS.between(hoy, fecha);
            // Un mes parcial cuenta como mes para aportar: faltan 40 dias -> 2 aportes.
            meses = (int) Math.max(0, mesesCompletos + (hoy.plusMonths(mesesCompletos).isBefore(fecha) ? 1 : 0));
            if (!cumplida && meses > 0) {
                aporte = faltante.divide(BigDecimal.valueOf(meses), 0, RoundingMode.UP);
            }
        }
        return new ProgresoMeta(meta, porcentaje, faltante, meses, aporte, cumplida, vencida);
    }
}
