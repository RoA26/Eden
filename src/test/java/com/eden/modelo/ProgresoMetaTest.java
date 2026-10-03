package com.eden.modelo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProgresoMetaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 24);

    private static Meta meta(String objetivo, String saldo, LocalDate fecha) {
        Meta m = new Meta();
        m.setMontoObjetivo(new BigDecimal(objetivo));
        m.setSaldoCajita(new BigDecimal(saldo));
        m.setFechaObjetivo(fecha);
        return m;
    }

    @Test
    void calculaAporteMensualRedondeandoHaciaArriba() {
        // Faltan 1.000.000 y hay 3 meses (24 sep -> 24 dic).
        ProgresoMeta p = ProgresoMeta.calcular(meta("1500000", "500000", LocalDate.of(2026, 12, 24)), HOY);

        assertThat(p.porcentaje()).isEqualTo(33);
        assertThat(p.faltante()).isEqualByComparingTo("1000000");
        assertThat(p.mesesRestantes()).isEqualTo(3);
        assertThat(p.aporteMensual()).isEqualByComparingTo("333334");
        assertThat(p.cumplida()).isFalse();
    }

    @Test
    void unMesParcialCuentaComoUnAporteMas() {
        ProgresoMeta p = ProgresoMeta.calcular(meta("100000", "0", LocalDate.of(2026, 11, 3)), HOY);
        assertThat(p.mesesRestantes()).isEqualTo(2);
    }

    @Test
    void metaCumplidaNoPideAportesYTopaEnCien() {
        ProgresoMeta p = ProgresoMeta.calcular(meta("100000", "150000", LocalDate.of(2027, 1, 1)), HOY);

        assertThat(p.cumplida()).isTrue();
        assertThat(p.porcentaje()).isEqualTo(100);
        assertThat(p.aporteMensual()).isNull();
    }

    @Test
    void sinFechaNoHayAporteSugeridoYFechaPasadaEsVencida() {
        assertThat(ProgresoMeta.calcular(meta("100000", "10000", null), HOY).aporteMensual()).isNull();
        assertThat(ProgresoMeta.calcular(meta("100000", "10000", LocalDate.of(2026, 9, 1)), HOY).vencida()).isTrue();
    }
}
