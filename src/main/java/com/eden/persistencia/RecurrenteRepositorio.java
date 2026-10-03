package com.eden.persistencia;

import com.eden.modelo.Recurrente;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurrenteRepositorio {

    Long insertar(Recurrente recurrente);

    void actualizar(Recurrente recurrente);

    void actualizarProximaFecha(Long idUsuario, Long id, LocalDate proximaFecha);

    void cambiarEstado(Long idUsuario, Long id, boolean activo);

    void eliminar(Long idUsuario, Long id);

    Optional<Recurrente> buscarPorId(Long idUsuario, Long id);

    List<Recurrente> listar(Long idUsuario);

    /** Activos cuya proxima fecha menos los dias de aviso ya llego. */
    List<Recurrente> listarPendientes(Long idUsuario, LocalDate hoy);

    boolean existeNombre(Long idUsuario, String nombre, Long idExcluido);
}
