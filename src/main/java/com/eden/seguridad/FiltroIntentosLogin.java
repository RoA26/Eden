package com.eden.seguridad;

import com.eden.modelo.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Corta el POST de /login antes de verificar la contrasena si esa IP (o
 * esa IP con ese usuario) esta bloqueada. Al no llegar a BCrypt, el
 * atacante tampoco consume CPU del servidor.
 *
 * No es un @Component a proposito: se agrega solo a la cadena de Spring
 * Security en SeguridadConfig, para que Spring Boot no lo registre dos veces.
 */
public class FiltroIntentosLogin extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroIntentosLogin.class);

    private final LimitadorIntentosLogin limitador;

    public FiltroIntentosLogin(LimitadorIntentosLogin limitador) {
        this.limitador = limitador;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return !("POST".equals(peticion.getMethod()) && "/login".equals(peticion.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String usuario = Usuario.normalizarNombreUsuario(peticion.getParameter("nombreUsuario"));
        String ip = peticion.getRemoteAddr();
        if (limitador.estaBloqueado(ip, usuario == null ? "" : usuario)) {
            log.warn("Inicio de sesión bloqueado temporalmente para ip={}", ip);
            respuesta.sendRedirect(peticion.getContextPath() + "/login?bloqueado");
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }
}
