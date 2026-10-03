package com.eden.modelo;

import com.eden.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DineroTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "85000        | 85000.00",
            "85.000       | 85000.00",
            "$ 85.000     | 85000.00",
            "1.250.000    | 1250000.00",
            "85.000,50    | 85000.50",
            "12.5         | 12.50",
            "1500,75      | 1500.75"
    })
    void interpretaElFormatoColombiano(String texto, String esperado) {
        assertThat(Dinero.parsear(texto, "monto")).isEqualByComparingTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5000", "abc", "12.345,678", " "})
    void rechazaMontosInvalidos(String texto) {
        assertThatThrownBy(() -> Dinero.parsear(texto, "monto"))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("monto");
    }

    @Test
    void formateaSinDecimalesConPuntoDeMiles() {
        assertThat(Dinero.formatear(new BigDecimal("1234567.40"))).isEqualTo("$ 1.234.567");
        assertThat(Dinero.formatear(null)).isEqualTo("$ 0");
        assertThat(Dinero.formatear(new BigDecimal("-2500"))).isEqualTo("-$ 2.500");
    }

    @Test
    void detectaTextosVaciosOCero() {
        assertThat(Dinero.esVacioOCero(null)).isTrue();
        assertThat(Dinero.esVacioOCero("$ 0.000")).isTrue();
        assertThat(Dinero.esVacioOCero("20.000")).isFalse();
    }
}
