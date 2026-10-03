package com.eden.persistencia;

import com.eden.modelo.RecuperacionContrasena;

import java.util.Optional;

public interface RecuperacionContrasenaRepositorio {

    void insertar(Long idUsuario, String pinHash, int minutosVigencia);

    /** true si el usuario pidio un PIN hace menos de {@code segundos} (frena solicitudes repetidas). */
    boolean existeSolicitudReciente(Long idUsuario, int segundos);

    /** Invalida los PIN anteriores: solo el ultimo PIN generado sirve. */
    void anularPendientes(Long idUsuario);

    /** Solicitud no usada, no vencida y con intentos disponibles; bloquea la fila hasta terminar la transaccion. */
    Optional<RecuperacionContrasena> buscarVigenteParaActualizar(Long idUsuario, int maximoIntentos);

    /** Suma un intento fallido y devuelve el total. */
    int registrarIntentoFallido(Long idSolicitud);

    void marcarUsada(Long idSolicitud);
}
