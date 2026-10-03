package com.eden.servicio;

import com.eden.dto.RestablecerContrasenaForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.RecuperacionContrasena;
import com.eden.modelo.Usuario;
import com.eden.persistencia.RecuperacionContrasenaRepositorio;
import com.eden.persistencia.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * "Olvide mi contrasena" sin servidor de correo: se genera un PIN de 6
 * digitos que solo aparece en la consola del servidor. Quien administra el
 * servidor se lo entrega al usuario (o lo lee, si es el mismo).
 *
 * Controles de seguridad:
 *  - El PIN se guarda solo como hash BCrypt, vence en 15 minutos y admite 5 intentos.
 *  - Un PIN nuevo anula los anteriores; no se genera otro antes de 60 segundos.
 *  - La respuesta al solicitar es identica exista o no el usuario (no revela cuentas).
 */
@Service
public class RecuperacionContrasenaService {

    static final int MINUTOS_VIGENCIA = 15;
    static final int MAXIMO_INTENTOS = 5;
    static final int SEGUNDOS_ENTRE_SOLICITUDES = 60;

    private static final Logger log = LoggerFactory.getLogger(RecuperacionContrasenaService.class);
    private static final String MENSAJE_PIN_INVALIDO = "El PIN no es válido o ya venció. Solicita uno nuevo.";

    private final SecureRandom aleatorio = new SecureRandom();
    private final UsuarioRepositorio usuarioRepositorio;
    private final RecuperacionContrasenaRepositorio recuperacionRepositorio;
    private final PasswordEncoder codificador;

    public RecuperacionContrasenaService(UsuarioRepositorio usuarioRepositorio,
                                         RecuperacionContrasenaRepositorio recuperacionRepositorio,
                                         PasswordEncoder codificador) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.recuperacionRepositorio = recuperacionRepositorio;
        this.codificador = codificador;
    }

    /** Genera un PIN y lo imprime en la consola. No informa al llamador si el usuario existe. */
    @Transactional
    public void solicitar(String nombreUsuarioIngresado) {
        String nombreUsuario = Usuario.normalizarNombreUsuario(nombreUsuarioIngresado);
        Optional<Usuario> usuario = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario).filter(Usuario::isActivo);
        if (usuario.isEmpty()) {
            log.info("Recuperación solicitada para una cuenta inexistente o inactiva: '{}'", paraLog(nombreUsuario));
            return;
        }
        Long idUsuario = usuario.get().getId();
        if (recuperacionRepositorio.existeSolicitudReciente(idUsuario, SEGUNDOS_ENTRE_SOLICITUDES)) {
            log.info("Recuperación ignorada para '{}': ya se generó un PIN hace menos de {} segundos.",
                    paraLog(nombreUsuario), SEGUNDOS_ENTRE_SOLICITUDES);
            return;
        }

        recuperacionRepositorio.anularPendientes(idUsuario);
        String pin = generarPin();
        recuperacionRepositorio.insertar(idUsuario, codificador.encode(pin), MINUTOS_VIGENCIA);

        log.warn("""

                ==================== EDEN: RECUPERACIÓN DE CONTRASEÑA ====================
                  Usuario: {}
                  PIN:     {}
                  Vence en {} minutos. Máximo {} intentos.
                ==========================================================================""",
                paraLog(nombreUsuario), pin, MINUTOS_VIGENCIA, MAXIMO_INTENTOS);
    }

    /**
     * Valida el PIN y cambia la contrasena. noRollbackFor: el intento fallido
     * debe quedar guardado aunque se lance la excepcion de negocio; si no,
     * el limite de intentos no serviria.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public void restablecer(RestablecerContrasenaForm formulario) {
        if (!formulario.getContrasena().equals(formulario.getConfirmacionContrasena())) {
            throw new ReglaNegocioException("confirmacionContrasena", "Las contraseñas no coinciden.");
        }

        String nombreUsuario = Usuario.normalizarNombreUsuario(formulario.getNombreUsuario());
        Usuario usuario = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ReglaNegocioException("pin", MENSAJE_PIN_INVALIDO));
        RecuperacionContrasena solicitud = recuperacionRepositorio
                .buscarVigenteParaActualizar(usuario.getId(), MAXIMO_INTENTOS)
                .orElseThrow(() -> new ReglaNegocioException("pin", MENSAJE_PIN_INVALIDO));

        if (!codificador.matches(formulario.getPin().trim(), solicitud.pinHash())) {
            int intentos = recuperacionRepositorio.registrarIntentoFallido(solicitud.id());
            int restantes = MAXIMO_INTENTOS - intentos;
            log.info("PIN incorrecto para el usuario id={} (intento {} de {}).", usuario.getId(), intentos, MAXIMO_INTENTOS);
            if (restantes <= 0) {
                recuperacionRepositorio.marcarUsada(solicitud.id());
                throw new ReglaNegocioException("pin", "Agotaste los intentos. Solicita un PIN nuevo.");
            }
            throw new ReglaNegocioException("pin", "PIN incorrecto. Te quedan " + restantes
                    + (restantes == 1 ? " intento." : " intentos."));
        }

        recuperacionRepositorio.marcarUsada(solicitud.id());
        usuarioRepositorio.actualizarContrasena(usuario.getId(), codificador.encode(formulario.getContrasena()));
        log.info("Contraseña restablecida para el usuario id={}.", usuario.getId());
    }

    private String generarPin() {
        return String.format("%06d", aleatorio.nextInt(1_000_000));
    }

    /** Evita que un texto con saltos de linea falsifique entradas en el log. */
    private static String paraLog(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\r\\n\\t]", "_");
    }
}
