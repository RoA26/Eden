package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.EstadoPresupuesto;
import com.eden.dto.PresupuestoForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Categoria;
import com.eden.modelo.Dinero;
import com.eden.modelo.TipoCategoria;
import com.eden.persistencia.PresupuestoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

/** Topes mensuales por categoria de gasto o costo, comparados con lo gastado. */
@Service
public class PresupuestoService {

    private final PresupuestoRepositorio presupuestoRepositorio;
    private final CategoriaService categoriaService;
    private final Clock reloj;

    public PresupuestoService(PresupuestoRepositorio presupuestoRepositorio, CategoriaService categoriaService,
                              Clock reloj) {
        this.presupuestoRepositorio = presupuestoRepositorio;
        this.categoriaService = categoriaService;
        this.reloj = reloj;
    }

    public YearMonth mesActual() {
        return YearMonth.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }

    public List<EstadoPresupuesto> estado(Long idUsuario, YearMonth mes) {
        return presupuestoRepositorio.listarConGastado(idUsuario, mes.atDay(1), mes.atEndOfMonth());
    }

    /** Presupuestos del mes actual que ya pasaron el 80% o se excedieron. */
    public List<EstadoPresupuesto> alertasDelMes(Long idUsuario) {
        return estado(idUsuario, mesActual()).stream()
                .filter(e -> e.excedido() || e.enAlerta())
                .toList();
    }

    @Transactional
    public void guardar(Long idUsuario, PresupuestoForm formulario) {
        Categoria categoria;
        try {
            categoria = categoriaService.obtener(idUsuario, formulario.getIdCategoria());
        } catch (RecursoNoEncontradoException e) {
            throw new ReglaNegocioException("idCategoria", "Esa categoría no existe.");
        }
        if (categoria.getTipo() == TipoCategoria.INGRESO) {
            throw new ReglaNegocioException("idCategoria", "Los presupuestos son para categorías de gasto o costo.");
        }
        BigDecimal monto = Dinero.parsear(formulario.getMonto(), "monto");
        presupuestoRepositorio.guardar(idUsuario, categoria.getId(), monto);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long idPresupuesto) {
        presupuestoRepositorio.eliminar(idUsuario, idPresupuesto);
    }

    public static BigDecimal total(List<EstadoPresupuesto> estados, boolean gastado) {
        return estados.stream()
                .map(e -> gastado ? e.gastado() : e.presupuestado())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
