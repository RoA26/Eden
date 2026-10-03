package com.eden.modelo;

import com.eden.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Conversion de montos escritos por el usuario (RN-08). Acepta la forma
 * colombiana: "85000", "85.000", "$ 85.000", "85.000,50". Nunca devuelve
 * negativos ni cero: los montos siempre son positivos y el tipo de
 * movimiento decide si suma o resta.
 */
public final class Dinero {

    public static final int ESCALA = 2;
    private static final BigDecimal MAXIMO = new BigDecimal("999999999999.99");
    private static final Pattern MILES_CON_PUNTO = Pattern.compile("^\\d{1,3}(\\.\\d{3})+$");

    private Dinero() {
    }

    /** @throws ReglaNegocioException asociada a {@code campo} si el texto no es un monto valido. */
    public static BigDecimal parsear(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException(campo, "Escribe un monto.");
        }
        String limpio = texto.replace("$", "").replace("\u00a0", "").replace(" ", "").trim();

        if (limpio.contains(",")) {
            limpio = limpio.replace(".", "").replace(",", ".");
        } else if (MILES_CON_PUNTO.matcher(limpio).matches()) {
            limpio = limpio.replace(".", "");
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException(campo, "Escribe el monto solo con números, por ejemplo 85000 o 85.000.");
        }
        if (monto.signum() <= 0) {
            throw new ReglaNegocioException(campo, "El monto debe ser mayor que cero.");
        }
        if (monto.scale() > ESCALA) {
            throw new ReglaNegocioException(campo, "El monto admite máximo dos decimales.");
        }
        if (monto.compareTo(MAXIMO) > 0) {
            throw new ReglaNegocioException(campo, "El monto es demasiado grande.");
        }
        return monto.setScale(ESCALA, RoundingMode.UNNECESSARY);
    }

    /** Igual que {@link #parsear} pero devuelve null si el texto esta vacio (campos opcionales). */
    public static BigDecimal parsearOpcional(String texto, String campo) {
        return (texto == null || texto.isBlank()) ? null : parsear(texto, campo);
    }

    /** Formato colombiano sin decimales: "$ 1.234.567". */
    public static String formatear(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
        formato.setMaximumFractionDigits(0);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        BigDecimal valor = monto == null ? BigDecimal.ZERO : monto;
        String texto = formato.format(valor.abs());
        return (valor.signum() < 0 ? "-$ " : "$ ") + texto;
    }

    /** true si el texto esta vacio o solo contiene ceros (ej. "0", "0.000", "$ 0"). */
    public static boolean esVacioOCero(String texto) {
        return texto == null || texto.replaceAll("[^1-9]", "").isEmpty();
    }

    public static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(ESCALA);
    }
}
