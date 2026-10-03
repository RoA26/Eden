package com.eden.dto;

import com.eden.modelo.TipoCategoria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoPresupuestoTest {

    private static EstadoPresupuesto estado(String presupuestado, String gastado) {
        return new EstadoPresupuesto(1L, 1L, "Alimentación", TipoCategoria.GASTO,
                new BigDecimal(presupuestado), new BigDecimal(gastado));
    }

    @Test
    void alertaDesdeElOchentaPorCiento() {
        assertThat(estado("500000", "399000").enAlerta()).isFalse();
        assertThat(estado("500000", "400000").enAlerta()).isTrue();
    }

    @Test
    void excedidoNoCuentaComoAlertaYLaBarraTopaEnCien() {
        EstadoPresupuesto e = estado("500000", "650000");
        assertThat(e.excedido()).isTrue();
        assertThat(e.enAlerta()).isFalse();
        assertThat(e.porcentaje()).isEqualTo(130);
        assertThat(e.porcentajeBarra()).isEqualTo(100);
        assertThat(e.restante()).isEqualByComparingTo("-150000");
    }
}
