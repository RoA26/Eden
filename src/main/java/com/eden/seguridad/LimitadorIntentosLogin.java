package com.eden.seguridad;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena los ataques de fuerza bruta contra el inicio de sesion.
 *
 * Lleva dos contadores en memoria:
 *  - por IP + usuario: 5 fallos en 15 minutos bloquean esa combinacion 15 minutos;
 *  - por IP: 20 fallos (cualquier usuario) bloquean esa IP 15 minutos.
 * Bloquear por IP + usuario, y no solo por usuario, evita que un extrano
 * pueda dejar por fuera al titular de la cuenta escribiendo mal su clave.
 *
 * Al estar en memoria, un reinicio de la app limpia los contadores; para
 * una aplicacion de una sola instancia es suficiente.
 */
@Component
public class LimitadorIntentosLogin {

    static final int MAXIMO_POR_USUARIO_E_IP = 5;
    static final int MAXIMO_POR_IP = 20;
    static final Duration VENTANA = Duration.ofMinutes(15);
    static final Duration BLOQUEO = Duration.ofMinutes(15);
    private static final int LIMITE_REGISTROS_ANTES_DE_LIMPIAR = 10_000;

    private record Registro(int fallos, Instant inicioVentana, Instant bloqueadoHasta) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Clock reloj;

    public LimitadorIntentosLogin(Clock reloj) {
        this.reloj = reloj;
    }

    public boolean estaBloqueado(String ip, String usuario) {
        Instant ahora = reloj.instant();
        return bloqueado(claveUsuario(ip, usuario), ahora) || bloqueado(claveIp(ip), ahora);
    }

    public void registrarFallo(String ip, String usuario) {
        Instant ahora = reloj.instant();
        limpiarSiHaceFalta(ahora);
        sumarFallo(claveUsuario(ip, usuario), MAXIMO_POR_USUARIO_E_IP, ahora);
        sumarFallo(claveIp(ip), MAXIMO_POR_IP, ahora);
    }

    /** Un inicio de sesion correcto borra los fallos de esa combinacion IP + usuario. */
    public void registrarExito(String ip, String usuario) {
        registros.remove(claveUsuario(ip, usuario));
    }

    private boolean bloqueado(String clave, Instant ahora) {
        Registro r = registros.get(clave);
        return r != null && r.bloqueadoHasta() != null && ahora.isBefore(r.bloqueadoHasta());
    }

    private void sumarFallo(String clave, int maximo, Instant ahora) {
        registros.compute(clave, (k, actual) -> {
            boolean ventanaVencida = actual == null || ahora.isAfter(actual.inicioVentana().plus(VENTANA));
            int fallos = ventanaVencida ? 1 : actual.fallos() + 1;
            Instant inicio = ventanaVencida ? ahora : actual.inicioVentana();
            Instant bloqueo = fallos >= maximo ? ahora.plus(BLOQUEO)
                    : (actual == null ? null : actual.bloqueadoHasta());
            return new Registro(fallos, inicio, bloqueo);
        });
    }

    /** Evita que la memoria crezca sin limite si llegan muchas IP distintas. */
    private void limpiarSiHaceFalta(Instant ahora) {
        if (registros.size() < LIMITE_REGISTROS_ANTES_DE_LIMPIAR) {
            return;
        }
        registros.entrySet().removeIf(e -> {
            Registro r = e.getValue();
            boolean ventanaVencida = ahora.isAfter(r.inicioVentana().plus(VENTANA));
            boolean sinBloqueo = r.bloqueadoHasta() == null || ahora.isAfter(r.bloqueadoHasta());
            return ventanaVencida && sinBloqueo;
        });
    }

    private static String claveUsuario(String ip, String usuario) {
        return "u|" + ip + "|" + usuario;
    }

    private static String claveIp(String ip) {
        return "ip|" + ip;
    }
}
