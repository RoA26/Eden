package com.eden.servicio;

import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.AccesoCompartido;
import com.eden.modelo.Usuario;
import com.eden.persistencia.AccesoCompartidoRepositorio;
import com.eden.persistencia.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Acceso de solo lectura entre cuentas. El titular concede y revoca; el
 * invitado solo puede consultar. Toda vista compartida debe pasar primero
 * por {@link #verificar(Long, Long)}.
 */
@Service
public class AccesoCompartidoService {

    private final AccesoCompartidoRepositorio accesoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public AccesoCompartidoService(AccesoCompartidoRepositorio accesoRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.accesoRepositorio = accesoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<AccesoCompartido> concedidos(Long idTitular) {
        return accesoRepositorio.listarConcedidos(idTitular);
    }

    public List<AccesoCompartido> recibidos(Long idInvitado) {
        return accesoRepositorio.listarRecibidos(idInvitado);
    }

    @Transactional
    public void conceder(Long idTitular, String nombreUsuarioInvitado) {
        String nombreUsuario = Usuario.normalizarNombreUsuario(nombreUsuarioInvitado);
        // Mismo mensaje si no existe o esta inactivo: no revelamos que cuentas existen.
        Usuario invitado = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ReglaNegocioException("nombreUsuario",
                        "No encontramos una cuenta activa con ese nombre de usuario."));
        if (invitado.getId().equals(idTitular)) {
            throw new ReglaNegocioException("nombreUsuario", "Ese es tu propio usuario.");
        }
        if (accesoRepositorio.buscar(idTitular, invitado.getId()).isPresent()) {
            throw new ReglaNegocioException("nombreUsuario", "Esa persona ya puede ver tu cuenta.");
        }
        accesoRepositorio.conceder(idTitular, invitado.getId());
    }

    @Transactional
    public void revocar(Long idTitular, Long idAcceso) {
        accesoRepositorio.revocar(idTitular, idAcceso);
    }

    /**
     * Comprueba que el invitado puede ver la cuenta del titular. Si no, responde
     * como si la cuenta no existiera (404), sin dar pistas.
     */
    public AccesoCompartido verificar(Long idInvitado, Long idTitular) {
        return accesoRepositorio.buscar(idTitular, idInvitado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));
    }
}
