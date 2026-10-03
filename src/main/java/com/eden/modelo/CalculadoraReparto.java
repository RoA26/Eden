package com.eden.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica pura del reparto sugerido (RN-04), sin acceso a datos, para
 * poder probarla de forma aislada.
 *
 * Cada cajita con porcentaje recibe su parte redondeada hacia abajo a
 * pesos enteros; la cajita "resto" recibe todo lo que sobra, incluidos
 * los pesos del redondeo. Si no hay cajita resto, lo que sobra se queda
 * en el disponible.
 */
public final class CalculadoraReparto {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private CalculadoraReparto() {
    }

    public record Linea(Cajita cajita, BigDecimal monto) {
    }

    public record Propuesta(BigDecimal montoARepartir, List<Linea> lineas, BigDecimal sobrante) {
    }

    public static Propuesta proponer(BigDecimal montoARepartir, List<Cajita> cajitasActivas) {
        List<Linea> lineas = new ArrayList<>();
        BigDecimal asignado = BigDecimal.ZERO;
        Cajita resto = null;

        for (Cajita cajita : cajitasActivas) {
            if (cajita.isEsResto()) {
                resto = cajita;
                continue;
            }
            BigDecimal parte = montoARepartir.multiply(cajita.getPorcentaje())
                    .divide(CIEN, 0, RoundingMode.DOWN)
                    .setScale(Dinero.ESCALA);
            lineas.add(new Linea(cajita, parte));
            asignado = asignado.add(parte);
        }

        BigDecimal sobrante = montoARepartir.subtract(asignado).max(BigDecimal.ZERO).setScale(Dinero.ESCALA);
        if (resto != null) {
            lineas.add(new Linea(resto, sobrante));
            sobrante = Dinero.cero();
        }
        return new Propuesta(montoARepartir.setScale(Dinero.ESCALA), lineas, sobrante);
    }
}
