package com.eden.persistencia;

import com.eden.modelo.Meta;

import java.util.List;
import java.util.Optional;

public interface MetaRepositorio {

    Long insertar(Meta meta);

    void actualizar(Meta meta);

    void cambiarEstado(Long idUsuario, Long id, boolean activa);

    void eliminar(Long idUsuario, Long id);

    Optional<Meta> buscarPorId(Long idUsuario, Long id);

    /** Metas con el nombre y el saldo actual de su cajita. */
    List<Meta> listar(Long idUsuario);

    boolean existeNombre(Long idUsuario, String nombre, Long idExcluido);
}
