package com.eden.persistencia;

import com.eden.modelo.Atajo;

import java.util.List;
import java.util.Optional;

public interface AtajoRepositorio {

    Long insertar(Atajo atajo);

    void eliminar(Long idUsuario, Long id);

    Optional<Atajo> buscarPorId(Long idUsuario, Long id);

    /** En orden de creacion: los botones no cambian de lugar entre un toque y otro. */
    List<Atajo> listar(Long idUsuario);

    int contar(Long idUsuario);

    boolean existeNombre(Long idUsuario, String nombre);
}
