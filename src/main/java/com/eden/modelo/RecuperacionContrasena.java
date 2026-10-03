package com.eden.modelo;

/** Solicitud vigente de recuperacion de contrasena (el PIN solo se guarda como hash). */
public record RecuperacionContrasena(Long id, Long idUsuario, String pinHash, int intentos) {
}
