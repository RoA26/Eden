package com.eden.persistencia;

import com.eden.modelo.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio {

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    boolean existeNombreUsuario(String nombreUsuario);

    /** Inserta el usuario y devuelve el id generado. */
    Long insertar(Usuario usuario);

    /** Reemplaza el hash de la contrasena (ya codificado con BCrypt). */
    void actualizarContrasena(Long idUsuario, String contrasenaHash);
}
