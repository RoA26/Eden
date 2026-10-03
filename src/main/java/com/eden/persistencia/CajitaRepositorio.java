package com.eden.persistencia;

import com.eden.modelo.Cajita;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CajitaRepositorio {

    Long insertar(Cajita cajita);

    void actualizar(Cajita cajita);

    void cambiarEstado(Long idUsuario, Long id, boolean activa);

    /** Quita la marca de "resto" a las demas cajitas del usuario. */
    void desmarcarResto(Long idUsuario, Long idExcluido);

    Optional<Cajita> buscarPorId(Long idUsuario, Long id);

    /** Cajitas con su saldo calculado desde los movimientos. */
    List<Cajita> listar(Long idUsuario);

    boolean existeNombre(Long idUsuario, String nombre, Long idExcluido);

    BigDecimal saldo(Long idUsuario, Long idCajita);
}
