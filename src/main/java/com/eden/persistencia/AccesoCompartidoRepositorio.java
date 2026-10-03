package com.eden.persistencia;

import com.eden.modelo.AccesoCompartido;

import java.util.List;
import java.util.Optional;

public interface AccesoCompartidoRepositorio {

    void conceder(Long idTitular, Long idInvitado);

    void revocar(Long idTitular, Long idAcceso);

    /** Personas a las que el titular les dio acceso. */
    List<AccesoCompartido> listarConcedidos(Long idTitular);

    /** Cuentas que el invitado puede ver. */
    List<AccesoCompartido> listarRecibidos(Long idInvitado);

    Optional<AccesoCompartido> buscar(Long idTitular, Long idInvitado);
}
