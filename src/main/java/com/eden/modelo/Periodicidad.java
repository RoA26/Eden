package com.eden.modelo;

import java.time.LocalDate;

/**
 * Cada cuanto se repite un recurrente. Mensual y anual usan un "dia ancla"
 * para no desplazarse: un pago del 31 cae el 28/29 en febrero y vuelve al
 * 31 en marzo.
 */
public enum Periodicidad {
    SEMANAL("Cada semana"),
    QUINCENAL("Cada 15 días"),
    MENSUAL("Cada mes"),
    ANUAL("Cada año");

    private final String etiqueta;

    Periodicidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public LocalDate siguiente(LocalDate fecha, int diaAncla) {
        return switch (this) {
            case SEMANAL -> fecha.plusWeeks(1);
            case QUINCENAL -> fecha.plusDays(15);
            case MENSUAL -> anclar(fecha.plusMonths(1), diaAncla);
            case ANUAL -> anclar(fecha.plusYears(1), diaAncla);
        };
    }

    private static LocalDate anclar(LocalDate fecha, int diaAncla) {
        return fecha.withDayOfMonth(Math.min(diaAncla, fecha.lengthOfMonth()));
    }
}
