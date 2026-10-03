package com.eden.persistencia;

import com.eden.modelo.FuenteIngreso;

import java.util.List;
import java.util.Optional;

public interface FuenteIngresoRepositorio {

    Long insertar(FuenteIngreso fuente);

    void actualizar(FuenteIngreso fuente);

    void cambiarEstado(Long idUsuario, Long id, boolean activa);

    Optional<FuenteIngreso> buscarPorId(Long idUsuario, Long id);

    List<FuenteIngreso> listar(Long idUsuario);

    boolean existeNombre(Long idUsuario, String nombre, Long idExcluido);
}
