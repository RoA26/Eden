package com.eden.web;

import com.eden.modelo.Dinero;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Formatos de presentacion usados desde las plantillas como
 * {@code ${@formato.pesos(valor)}}, para no repetir logica de formato en las vistas.
 */
@Component("formato")
public class FormatoVista {

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("EEE d MMM", ES_CO);
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES_CO);

    public String pesos(BigDecimal monto) {
        return Dinero.formatear(monto);
    }

    /** Monto sin decimales ni separadores, para prellenar campos: 85000.00 -> "85000". */
    public String entero(BigDecimal monto) {
        return monto == null ? "" : monto.setScale(0, java.math.RoundingMode.DOWN).toPlainString();
    }

    public String porcentaje(BigDecimal valor) {
        return valor == null ? "0%" : valor.stripTrailingZeros().toPlainString() + "%";
    }

    public String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA_CORTA.format(fecha);
    }

    public String fechaLarga(LocalDate fecha) {
        return fecha == null ? "" : FECHA_LARGA.format(fecha);
    }

    public String mes(YearMonth mes) {
        return mes.getMonth().getDisplayName(TextStyle.FULL, ES_CO) + " " + mes.getYear();
    }

    /** Montos compactos para celdas pequenas del calendario: 85000 -> "85k", 1250000 -> "1,3M". */
    public String compacto(BigDecimal monto) {
        if (monto == null || monto.signum() == 0) {
            return "";
        }
        double valor = monto.doubleValue();
        if (valor >= 1_000_000) {
            return String.format(ES_CO, "%.1fM", valor / 1_000_000).replace(",0M", "M");
        }
        if (valor >= 1_000) {
            return Math.round(valor / 1_000) + "k";
        }
        return String.valueOf(Math.round(valor));
    }
}
