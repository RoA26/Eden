package com.eden.servicio;

import com.eden.dto.AtajoForm;
import com.eden.dto.MovimientoForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Atajo;
import com.eden.modelo.Movimiento;
import com.eden.modelo.TipoMovimiento;
import com.eden.persistencia.AtajoRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtajoServiceTest {

    private static final Long USUARIO = 7L;
    private static final LocalDate HOY = LocalDate.of(2026, 10, 4);

    @Mock private AtajoRepositorio atajoRepositorio;
    @Mock private MovimientoService movimientoService;

    private AtajoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AtajoService(atajoRepositorio, movimientoService);
    }

    @Test
    void creaElBotonConLosDatosNormalizadosPorLasReglasDeMovimientos() {
        when(movimientoService.hoy()).thenReturn(HOY);
        Movimiento validado = new Movimiento();
        validado.setTipo(TipoMovimiento.GASTO);
        validado.setMonto(new BigDecimal("5000.00"));
        validado.setIdCategoria(3L);
        validado.setIdFuente(null); // los gastos personales no llevan fuente
        when(movimientoService.validar(eq(USUARIO), any(MovimientoForm.class))).thenReturn(validado);
        when(atajoRepositorio.insertar(any(Atajo.class))).thenReturn(11L);

        Atajo atajo = servicio.crear(USUARIO, formulario("  Moto  ", "5.000"));

        ArgumentCaptor<Atajo> guardado = ArgumentCaptor.forClass(Atajo.class);
        verify(atajoRepositorio).insertar(guardado.capture());
        assertThat(guardado.getValue().getNombre()).isEqualTo("Moto");
        assertThat(guardado.getValue().getMonto()).isEqualByComparingTo("5000");
        assertThat(guardado.getValue().getIdFuente()).isNull();
        assertThat(atajo.getId()).isEqualTo(11L);
    }

    @Test
    void rechazaNombresRepetidos() {
        when(atajoRepositorio.existeNombre(USUARIO, "Moto")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(USUARIO, formulario("Moto", "5000")))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("nombre");
        verify(atajoRepositorio, never()).insertar(any());
    }

    @Test
    void limitaLaCantidadDeBotones() {
        when(atajoRepositorio.contar(USUARIO)).thenReturn(AtajoService.MAXIMO_POR_USUARIO);

        assertThatThrownBy(() -> servicio.crear(USUARIO, formulario("Otro", "1000")))
                .isInstanceOf(ReglaNegocioException.class);
        verifyNoInteractions(movimientoService);
    }

    @Test
    void usarRegistraElMovimientoConLaFechaDeHoyYElNombreComoDescripcion() {
        Atajo atajo = new Atajo();
        atajo.setId(11L);
        atajo.setNombre("Venta postre");
        atajo.setTipo(TipoMovimiento.INGRESO);
        atajo.setMonto(new BigDecimal("12000.00"));
        atajo.setIdCategoria(2L);
        when(atajoRepositorio.buscarPorId(USUARIO, 11L)).thenReturn(Optional.of(atajo));
        when(movimientoService.hoy()).thenReturn(HOY);
        when(movimientoService.registrar(eq(USUARIO), any(MovimientoForm.class))).thenReturn(99L);

        Long idMovimiento = servicio.usar(USUARIO, 11L);

        ArgumentCaptor<MovimientoForm> registrado = ArgumentCaptor.forClass(MovimientoForm.class);
        verify(movimientoService).registrar(eq(USUARIO), registrado.capture());
        assertThat(registrado.getValue().getTipo()).isEqualTo(TipoMovimiento.INGRESO);
        assertThat(registrado.getValue().getFecha()).isEqualTo(HOY);
        assertThat(registrado.getValue().getMonto()).isEqualTo("12000.00");
        assertThat(registrado.getValue().getDescripcion()).isEqualTo("Venta postre");
        assertThat(idMovimiento).isEqualTo(99L);
    }

    @Test
    void noUsaNiEliminaBotonesDeOtroUsuario() {
        when(atajoRepositorio.buscarPorId(USUARIO, 50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.usar(USUARIO, 50L)).isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> servicio.eliminar(USUARIO, 50L)).isInstanceOf(RecursoNoEncontradoException.class);
        verify(atajoRepositorio, never()).eliminar(any(), any());
        verifyNoInteractions(movimientoService);
    }

    private static AtajoForm formulario(String nombre, String monto) {
        AtajoForm f = new AtajoForm();
        f.setNombre(nombre);
        f.setTipo(TipoMovimiento.GASTO);
        f.setMonto(monto);
        f.setIdCategoria(3L);
        return f;
    }
}
