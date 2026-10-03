package com.eden.servicio;

import com.eden.dto.RestablecerContrasenaForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.RecuperacionContrasena;
import com.eden.modelo.Usuario;
import com.eden.persistencia.RecuperacionContrasenaRepositorio;
import com.eden.persistencia.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaServiceTest {

    @Mock private UsuarioRepositorio usuarioRepositorio;
    @Mock private RecuperacionContrasenaRepositorio recuperacionRepositorio;

    // Codificador real con costo bajo: las pruebas verifican hashes de verdad y siguen siendo rapidas.
    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);
    private RecuperacionContrasenaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RecuperacionContrasenaService(usuarioRepositorio, recuperacionRepositorio, codificador);
    }

    private static Usuario usuarioActivo() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setNombreUsuario("roger");
        u.setActivo(true);
        return u;
    }

    private RestablecerContrasenaForm formulario(String pin) {
        RestablecerContrasenaForm f = new RestablecerContrasenaForm();
        f.setNombreUsuario("  Roger ");
        f.setPin(pin);
        f.setContrasena("NuevaClave2026");
        f.setConfirmacionContrasena("NuevaClave2026");
        return f;
    }

    @Test
    void solicitarParaUsuarioInexistenteNoGeneraNada() {
        when(usuarioRepositorio.buscarPorNombreUsuario("nadie")).thenReturn(Optional.empty());

        servicio.solicitar("Nadie");

        verify(recuperacionRepositorio, never()).insertar(anyLong(), anyString(), anyInt());
    }

    @Test
    void solicitarGuardaSoloElHashDeUnPinDeSeisDigitos() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.existeSolicitudReciente(7L, 60)).thenReturn(false);

        servicio.solicitar(" Roger ");

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(recuperacionRepositorio).anularPendientes(7L);
        verify(recuperacionRepositorio).insertar(eq(7L), hash.capture(), eq(15));
        assertThat(hash.getValue()).startsWith("$2a$").doesNotMatch("^\\d{6}$");
    }

    @Test
    void solicitudRepetidaEnMenosDeUnMinutoSeIgnora() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.existeSolicitudReciente(7L, 60)).thenReturn(true);

        servicio.solicitar("roger");

        verify(recuperacionRepositorio, never()).insertar(anyLong(), anyString(), anyInt());
    }

    @Test
    void pinCorrectoCambiaLaContrasenaYConsumeLaSolicitud() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.buscarVigenteParaActualizar(7L, 5))
                .thenReturn(Optional.of(new RecuperacionContrasena(3L, 7L, codificador.encode("482913"), 0)));

        servicio.restablecer(formulario(" 482913 "));

        ArgumentCaptor<String> nuevoHash = ArgumentCaptor.forClass(String.class);
        verify(recuperacionRepositorio).marcarUsada(3L);
        verify(usuarioRepositorio).actualizarContrasena(eq(7L), nuevoHash.capture());
        assertThat(codificador.matches("NuevaClave2026", nuevoHash.getValue())).isTrue();
    }

    @Test
    void pinIncorrectoSumaIntentoYAvisaCuantosQuedan() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.buscarVigenteParaActualizar(7L, 5))
                .thenReturn(Optional.of(new RecuperacionContrasena(3L, 7L, codificador.encode("482913"), 1)));
        when(recuperacionRepositorio.registrarIntentoFallido(3L)).thenReturn(2);

        assertThatThrownBy(() -> servicio.restablecer(formulario("000000")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Te quedan 3 intentos");
        verify(usuarioRepositorio, never()).actualizarContrasena(anyLong(), anyString());
    }

    @Test
    void alQuintoFalloLaSolicitudQuedaAnulada() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.buscarVigenteParaActualizar(7L, 5))
                .thenReturn(Optional.of(new RecuperacionContrasena(3L, 7L, codificador.encode("482913"), 4)));
        when(recuperacionRepositorio.registrarIntentoFallido(3L)).thenReturn(5);

        assertThatThrownBy(() -> servicio.restablecer(formulario("111111")))
                .hasMessageContaining("Agotaste los intentos");
        verify(recuperacionRepositorio).marcarUsada(3L);
    }

    @Test
    void sinSolicitudVigenteResponderConMensajeGenerico() {
        when(usuarioRepositorio.buscarPorNombreUsuario("roger")).thenReturn(Optional.of(usuarioActivo()));
        when(recuperacionRepositorio.buscarVigenteParaActualizar(7L, 5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.restablecer(formulario("482913")))
                .hasMessageContaining("no es válido o ya venció");
    }

    @Test
    void contrasenasDistintasSeRechazanAntesDeTocarElPin() {
        RestablecerContrasenaForm f = formulario("482913");
        f.setConfirmacionContrasena("OtraDistinta");

        assertThatThrownBy(() -> servicio.restablecer(f))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("confirmacionContrasena");
        verify(recuperacionRepositorio, never()).buscarVigenteParaActualizar(anyLong(), anyInt());
    }
}
