package com.eden.servicio;

import com.eden.config.PropiedadesApp;
import com.eden.dto.RegistroUsuarioForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Usuario;
import com.eden.persistencia.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceTest {

    @Mock private UsuarioRepositorio usuarioRepositorio;
    @Mock private CategoriaService categoriaService;
    @Mock private PasswordEncoder codificador;

    private RegistroUsuarioService servicio;

    @BeforeEach
    void preparar() {
        PropiedadesApp propiedades = new PropiedadesApp("Eden", "codigo-secreto");
        servicio = new RegistroUsuarioService(usuarioRepositorio, categoriaService, codificador, propiedades);
    }

    @Test
    void rechazaCodigoDeInvitacionInvalido() {
        RegistroUsuarioForm formulario = formularioValido();
        formulario.setCodigoInvitacion("otro-codigo");

        assertThatThrownBy(() -> servicio.registrar(formulario))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("codigoInvitacion");
        verifyNoInteractions(usuarioRepositorio, categoriaService);
    }

    @Test
    void rechazaContrasenasQueNoCoinciden() {
        RegistroUsuarioForm formulario = formularioValido();
        formulario.setConfirmacionContrasena("diferente123");

        assertThatThrownBy(() -> servicio.registrar(formulario))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("confirmacionContrasena");
    }

    @Test
    void rechazaNombreDeUsuarioExistente() {
        when(usuarioRepositorio.existeNombreUsuario("roger")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(formularioValido()))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("campo").isEqualTo("nombreUsuario");
        verify(usuarioRepositorio, never()).insertar(any());
    }

    @Test
    void traduceRegistroSimultaneoAErrorDeNegocio() {
        when(usuarioRepositorio.existeNombreUsuario("roger")).thenReturn(false);
        when(codificador.encode("claveSegura1")).thenReturn("hash");
        when(usuarioRepositorio.insertar(any())).thenThrow(new DuplicateKeyException("duplicado"));

        assertThatThrownBy(() -> servicio.registrar(formularioValido()))
                .isInstanceOf(ReglaNegocioException.class);
        verifyNoInteractions(categoriaService);
    }

    @Test
    void registraUsuarioNormalizadoYCreaSusCategorias() {
        when(usuarioRepositorio.existeNombreUsuario("roger")).thenReturn(false);
        when(codificador.encode("claveSegura1")).thenReturn("hash-bcrypt");
        when(usuarioRepositorio.insertar(any())).thenReturn(7L);

        Long id = servicio.registrar(formularioValido());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositorio).insertar(captor.capture());
        assertThat(id).isEqualTo(7L);
        assertThat(captor.getValue().getNombreUsuario()).isEqualTo("roger");
        assertThat(captor.getValue().getContrasenaHash()).isEqualTo("hash-bcrypt");
        verify(categoriaService).crearCategoriasPorDefecto(7L);
    }

    private RegistroUsuarioForm formularioValido() {
        RegistroUsuarioForm formulario = new RegistroUsuarioForm();
        formulario.setNombreCompleto("  Roger Bolaño ");
        formulario.setNombreUsuario(" Roger ");
        formulario.setContrasena("claveSegura1");
        formulario.setConfirmacionContrasena("claveSegura1");
        formulario.setCodigoInvitacion("codigo-secreto");
        return formulario;
    }
}
