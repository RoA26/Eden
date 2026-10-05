package com.eden.web;

import com.eden.seguridad.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** La landing (/) se muestra siempre: con o sin sesion, nunca redirige. */
class InicioControllerTest {

    private InicioController controlador;

    @BeforeEach
    void preparar() {
        controlador = new InicioController(null, null, null, null, null, null, null);
    }

    @Test
    void sinSesionMuestraLaLanding() {
        ExtendedModelMap modelo = new ExtendedModelMap();

        String vista = controlador.raiz(null, modelo);

        assertThat(vista).isEqualTo(InicioController.VISTA_LANDING);
        assertThat(modelo.get("sesionActiva")).isEqualTo(false);
    }

    @Test
    void conSesionTambienMuestraLaLandingSinRedirigir() {
        ExtendedModelMap modelo = new ExtendedModelMap();

        String vista = controlador.raiz(mock(UsuarioAutenticado.class), modelo);

        assertThat(vista).isEqualTo(InicioController.VISTA_LANDING).doesNotStartWith("redirect:");
        assertThat(modelo.get("sesionActiva")).isEqualTo(true);
    }
}
