package com.eden.modelo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRepartoTest {

    private static Cajita cajita(long id, String nombre, String porcentaje, boolean resto) {
        Cajita c = new Cajita();
        c.setId(id);
        c.setNombre(nombre);
        c.setPorcentaje(new BigDecimal(porcentaje));
        c.setEsResto(resto);
        return c;
    }

    @Test
    void elRestoRecibeLoQueSobraIncluidoElRedondeo() {
        List<Cajita> cajitas = List.of(
                cajita(1, "Daniela", "50", false),
                cajita(2, "Atenea", "33.33", false),
                cajita(3, "Emergencia", "10", false),
                cajita(4, "Chelas", "0", true));

        CalculadoraReparto.Propuesta p = CalculadoraReparto.proponer(new BigDecimal("100001"), cajitas);

        assertThat(p.lineas()).extracting(l -> l.cajita().getNombre())
                .containsExactly("Daniela", "Atenea", "Emergencia", "Chelas");
        assertThat(p.lineas().get(0).monto()).isEqualByComparingTo("50000");   // 50000.5 -> abajo
        assertThat(p.lineas().get(1).monto()).isEqualByComparingTo("33330");   // 33330.33 -> abajo
        assertThat(p.lineas().get(2).monto()).isEqualByComparingTo("10000");   // 10000.1 -> abajo
        assertThat(p.lineas().get(3).monto()).isEqualByComparingTo("6671");
        BigDecimal suma = p.lineas().stream().map(CalculadoraReparto.Linea::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(suma).isEqualByComparingTo("100001");
        assertThat(p.sobrante()).isEqualByComparingTo("0");
    }

    @Test
    void sinCajitaRestoElSobranteQuedaEnElDisponible() {
        List<Cajita> cajitas = List.of(cajita(1, "Daniela", "60", false), cajita(2, "Atenea", "30", false));

        CalculadoraReparto.Propuesta p = CalculadoraReparto.proponer(new BigDecimal("200000"), cajitas);

        assertThat(p.lineas()).hasSize(2);
        assertThat(p.sobrante()).isEqualByComparingTo("20000");
    }

    @Test
    void montoCeroNoGeneraAportes() {
        CalculadoraReparto.Propuesta p = CalculadoraReparto.proponer(BigDecimal.ZERO,
                List.of(cajita(1, "Daniela", "50", false), cajita(2, "Chelas", "0", true)));

        assertThat(p.lineas()).allSatisfy(l -> assertThat(l.monto()).isEqualByComparingTo("0"));
        assertThat(p.sobrante()).isEqualByComparingTo("0");
    }
}
