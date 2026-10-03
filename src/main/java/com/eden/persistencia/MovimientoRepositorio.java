package com.eden.persistencia;

import com.eden.dto.FiltroMovimientos;
import com.eden.dto.PaginaMovimientos;
import com.eden.dto.ResumenFuente;
import com.eden.dto.ResumenPeriodo;
import com.eden.dto.TotalPorCategoria;
import com.eden.dto.TotalPorDia;
import com.eden.dto.TotalPorMes;
import com.eden.modelo.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepositorio {

    Long insertar(Movimiento movimiento);

    /** Actualiza fecha, monto, categoria, fuente y descripcion de un ingreso o gasto. */
    void actualizar(Movimiento movimiento);

    /** Sincroniza fecha y monto del retiro que paga un gasto. */
    void actualizarRelacionado(Long idUsuario, Long idOrigen, LocalDate fecha, BigDecimal monto);

    void eliminar(Long idUsuario, Long id);

    Optional<Movimiento> buscarPorId(Long idUsuario, Long id);

    Optional<Movimiento> buscarRelacionado(Long idUsuario, Long idOrigen);

    PaginaMovimientos buscar(Long idUsuario, FiltroMovimientos filtro, int tamanoPagina);

    List<Movimiento> ultimos(Long idUsuario, int cantidad);

    List<Movimiento> listarPorCajita(Long idUsuario, Long idCajita, int cantidad);

    /** Disponible por repartir (RN-03). */
    BigDecimal disponible(Long idUsuario);

    ResumenPeriodo resumen(Long idUsuario, LocalDate desde, LocalDate hasta);

    List<TotalPorDia> totalesPorDia(Long idUsuario, LocalDate desde, LocalDate hasta);

    List<TotalPorMes> totalesPorMes(Long idUsuario, LocalDate desde, LocalDate hasta);

    List<TotalPorCategoria> gastosPorCategoria(Long idUsuario, LocalDate desde, LocalDate hasta);

    List<ResumenFuente> resumenPorFuente(Long idUsuario, LocalDate desde, LocalDate hasta);
}
