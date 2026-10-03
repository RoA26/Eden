package com.eden.servicio;

import com.eden.config.PropiedadesApp;
import com.eden.dto.RegistroUsuarioForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Usuario;
import com.eden.persistencia.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Registro de usuarios nuevos. Crea el usuario y sus categorias por
 * defecto en una misma transaccion: o queda todo creado o nada.
 */
@Service
public class RegistroUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(RegistroUsuarioService.class);
    private static final String MENSAJE_USUARIO_EN_USO = "Ese nombre de usuario ya está en uso.";

    private final UsuarioRepositorio usuarioRepositorio;
    private final CategoriaService categoriaService;
    private final PasswordEncoder codificadorContrasenas;
    private final PropiedadesApp propiedades;

    public RegistroUsuarioService(UsuarioRepositorio usuarioRepositorio,
                                  CategoriaService categoriaService,
                                  PasswordEncoder codificadorContrasenas,
                                  PropiedadesApp propiedades) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.categoriaService = categoriaService;
        this.codificadorContrasenas = codificadorContrasenas;
        this.propiedades = propiedades;
    }

    /**
     * @return id del usuario creado.
     * @throws ReglaNegocioException si el codigo de invitacion es invalido,
     *         las contrasenas no coinciden o el nombre de usuario ya existe.
     */
    @Transactional
    public Long registrar(RegistroUsuarioForm formulario) {
        validarCodigoInvitacion(formulario.getCodigoInvitacion());

        if (!formulario.getContrasena().equals(formulario.getConfirmacionContrasena())) {
            throw new ReglaNegocioException("confirmacionContrasena", "Las contraseñas no coinciden.");
        }

        String nombreUsuario = Usuario.normalizarNombreUsuario(formulario.getNombreUsuario());
        if (usuarioRepositorio.existeNombreUsuario(nombreUsuario)) {
            throw new ReglaNegocioException("nombreUsuario", MENSAJE_USUARIO_EN_USO);
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setNombreCompleto(formulario.getNombreCompleto().trim());
        usuario.setContrasenaHash(codificadorContrasenas.encode(formulario.getContrasena()));

        Long idUsuario;
        try {
            idUsuario = usuarioRepositorio.insertar(usuario);
        } catch (DuplicateKeyException e) {
            // Dos registros simultaneos con el mismo nombre: la restriccion UNIQUE decide.
            throw new ReglaNegocioException("nombreUsuario", MENSAJE_USUARIO_EN_USO);
        }

        categoriaService.crearCategoriasPorDefecto(idUsuario);
        log.info("Usuario registrado: id={}, nombreUsuario={}", idUsuario, nombreUsuario);
        return idUsuario;
    }

    /** Comparacion en tiempo constante para no filtrar informacion por tiempos de respuesta. */
    private void validarCodigoInvitacion(String codigoIngresado) {
        byte[] esperado = propiedades.codigoInvitacion().getBytes(StandardCharsets.UTF_8);
        byte[] recibido = (codigoIngresado == null ? "" : codigoIngresado.trim()).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperado, recibido)) {
            throw new ReglaNegocioException("codigoInvitacion", "El código de invitación no es válido.");
        }
    }
}
