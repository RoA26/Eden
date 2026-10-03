package com.eden.modelo;

import java.time.LocalDate;

/** Permiso de solo lectura que un titular concede a un invitado. */
public record AccesoCompartido(Long id, Long idTitular, String nombreCompletoTitular,
                               Long idInvitado, String nombreUsuarioInvitado, String nombreCompletoInvitado,
                               LocalDate fechaConcesion) {
}
