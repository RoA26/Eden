package com.eden.seguridad;

import com.eden.modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Escucha los eventos que publica Spring Security al autenticar y alimenta
 * al {@link LimitadorIntentosLogin}. Asi el conteo no se mezcla con la
 * configuracion de seguridad.
 */
@Component
public class EscuchaEventosAutenticacion {

    private static final Logger log = LoggerFactory.getLogger(EscuchaEventosAutenticacion.class);

    private final LimitadorIntentosLogin limitador;

    public EscuchaEventosAutenticacion(LimitadorIntentosLogin limitador) {
        this.limitador = limitador;
    }

    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        String usuario = usuario(evento.getAuthentication());
        String ip = ip(evento.getAuthentication());
        limitador.registrarFallo(ip, usuario);
        log.info("Inicio de sesión fallido: usuario='{}' ip={}", usuario.replaceAll("[\\r\\n\\t]", "_"), ip);
    }

    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        limitador.registrarExito(ip(evento.getAuthentication()), usuario(evento.getAuthentication()));
    }

    private static String usuario(Authentication autenticacion) {
        String nombre = Usuario.normalizarNombreUsuario(autenticacion.getName());
        return nombre == null ? "" : nombre;
    }

    static String ip(Authentication autenticacion) {
        return autenticacion.getDetails() instanceof WebAuthenticationDetails detalles
                && detalles.getRemoteAddress() != null
                ? detalles.getRemoteAddress() : "desconocida";
    }
}
