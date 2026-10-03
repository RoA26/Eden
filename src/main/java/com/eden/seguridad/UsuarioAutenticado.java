package com.eden.seguridad;

import com.eden.modelo.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario en sesion. Los controladores lo reciben con
 * {@code @AuthenticationPrincipal} y pasan su id a los servicios, que
 * filtran siempre por ese id (RN-07: aislamiento entre usuarios).
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final String contrasenaHash;
    private final boolean activo;

    private UsuarioAutenticado(Long id, String nombreUsuario, String nombreCompleto,
                               String contrasenaHash, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.contrasenaHash = contrasenaHash;
        this.activo = activo;
    }

    public static UsuarioAutenticado desde(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getNombreUsuario(),
                usuario.getNombreCompleto(), usuario.getContrasenaHash(), usuario.isActivo());
    }

    public Long getId() { return id; }

    public String getNombreCompleto() { return nombreCompleto; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USUARIO"));
    }

    @Override
    public String getPassword() { return contrasenaHash; }

    @Override
    public String getUsername() { return nombreUsuario; }

    @Override
    public boolean isEnabled() { return activo; }
}
