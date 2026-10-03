package com.eden.seguridad;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorIntentosLoginTest {

    /** Reloj que la prueba puede adelantar a voluntad. */
    private static final class RelojManual extends Clock {
        private Instant ahora = Instant.parse("2026-10-03T12:00:00Z");
        void avanzar(Duration d) { ahora = ahora.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zona) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    private final RelojManual reloj = new RelojManual();
    private final LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(reloj);

    @Test
    void bloqueaAlQuintoFalloYLiberaDespuesDeQuinceMinutos() {
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("1.2.3.4", "roger");
        }
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isFalse();

        limitador.registrarFallo("1.2.3.4", "roger");
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isTrue();

        reloj.avanzar(Duration.ofMinutes(15).plusSeconds(1));
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isFalse();
    }

    @Test
    void unAtacanteNoDejaPorFueraAlTitularDesdeOtraIp() {
        for (int i = 0; i < 5; i++) {
            limitador.registrarFallo("9.9.9.9", "roger");
        }
        assertThat(limitador.estaBloqueado("9.9.9.9", "roger")).isTrue();
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isFalse();
    }

    @Test
    void bloqueaLaIpQueProbaVeinteUsuariosDistintos() {
        for (int i = 0; i < 20; i++) {
            limitador.registrarFallo("9.9.9.9", "usuario" + i);
        }
        assertThat(limitador.estaBloqueado("9.9.9.9", "cualquiera")).isTrue();
    }

    @Test
    void losFallosViejosNoSeAcumulanYElExitoReiniciaElConteo() {
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("1.2.3.4", "roger");
        }
        reloj.avanzar(Duration.ofMinutes(16));
        limitador.registrarFallo("1.2.3.4", "roger");
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isFalse();

        limitador.registrarFallo("1.2.3.4", "roger");
        limitador.registrarExito("1.2.3.4", "roger");
        for (int i = 0; i < 4; i++) {
            limitador.registrarFallo("1.2.3.4", "roger");
        }
        assertThat(limitador.estaBloqueado("1.2.3.4", "roger")).isFalse();
    }
}
