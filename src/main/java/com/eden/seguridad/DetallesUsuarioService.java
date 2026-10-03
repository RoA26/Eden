package com.eden.seguridad;

import com.eden.modelo.Usuario;
import com.eden.persistencia.UsuarioRepositorio;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre Spring Security y nuestra capa de persistencia. La
 * verificacion de la contrasena (BCrypt) la hace Spring Security.
 */
@Service
public class DetallesUsuarioService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public DetallesUsuarioService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) {
        return usuarioRepositorio.buscarPorNombreUsuario(Usuario.normalizarNombreUsuario(nombreUsuario))
                .map(UsuarioAutenticado::desde)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
