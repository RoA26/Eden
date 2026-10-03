package com.eden.persistencia;

import com.eden.dto.EstadoPresupuesto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PresupuestoRepositorio {

    /** Crea el presupuesto de la categoria o reemplaza su monto si ya existia. */
    void guardar(Long idUsuario, Long idCategoria, BigDecimal montoMensual);

    void eliminar(Long idUsuario, Long id);

    /** Cada presupuesto con lo gastado en su categoria entre las fechas dadas. */
    List<EstadoPresupuesto> listarConGastado(Long idUsuario, LocalDate desde, LocalDate hasta);
}
