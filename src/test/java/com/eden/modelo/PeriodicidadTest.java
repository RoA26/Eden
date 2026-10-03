package com.eden.modelo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodicidadTest {

    @Test
    void mensualConservaElDiaAnclaAunqueFebreroSeaMasCorto() {
        LocalDate enero31 = LocalDate.of(2027, 1, 31);
        LocalDate febrero = Periodicidad.MENSUAL.siguiente(enero31, 31);
        LocalDate marzo = Periodicidad.MENSUAL.siguiente(febrero, 31);

        assertThat(febrero).isEqualTo(LocalDate.of(2027, 2, 28));
        assertThat(marzo).isEqualTo(LocalDate.of(2027, 3, 31));
    }

    @Test
    void semanalQuincenalYAnual() {
        LocalDate base = LocalDate.of(2026, 9, 24);
        assertThat(Periodicidad.SEMANAL.siguiente(base, 24)).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(Periodicidad.QUINCENAL.siguiente(base, 24)).isEqualTo(LocalDate.of(2026, 10, 9));
        assertThat(Periodicidad.ANUAL.siguiente(LocalDate.of(2028, 2, 29), 29)).isEqualTo(LocalDate.of(2029, 2, 28));
    }

    @Test
    void recurrenteQuedaPendienteDentroDeLosDiasDeAviso() {
        Recurrente r = new Recurrente();
        r.setProximaFecha(LocalDate.of(2026, 10, 1));
        r.setDiasAviso(3);

        assertThat(r.estaPendiente(LocalDate.of(2026, 9, 27))).isFalse();
        assertThat(r.estaPendiente(LocalDate.of(2026, 9, 28))).isTrue();
        assertThat(r.estaVencido(LocalDate.of(2026, 10, 2))).isTrue();
    }
}
