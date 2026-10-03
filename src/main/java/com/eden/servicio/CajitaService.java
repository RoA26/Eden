package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.CajitaForm;
import com.eden.dto.OperacionCajitaForm;
import com.eden.dto.PropuestaReparto;
import com.eden.dto.RepartoForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Cajita;
import com.eden.modelo.CalculadoraReparto;
import com.eden.modelo.Dinero;
import com.eden.modelo.Movimiento;
import com.eden.modelo.TipoMovimiento;
import com.eden.persistencia.CajitaRepositorio;
import com.eden.persistencia.MovimientoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cajitas de ahorro: porcentajes (RN-05), saldos calculados (RN-06) y
 * reparto del disponible (RN-03 y RN-04).
 */
@Service
public class CajitaService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CajitaRepositorio cajitaRepositorio;
    private final MovimientoRepositorio movimientoRepositorio;
    private final Clock reloj;

    public CajitaService(CajitaRepositorio cajitaRepositorio, MovimientoRepositorio movimientoRepositorio, Clock reloj) {
        this.cajitaRepositorio = cajitaRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ consultas

    public List<Cajita> listar(Long idUsuario) {
        return cajitaRepositorio.listar(idUsuario);
    }

    public List<Cajita> listarActivas(Long idUsuario) {
        return cajitaRepositorio.listar(idUsuario).stream().filter(Cajita::isActiva).toList();
    }

    public Cajita obtener(Long idUsuario, Long idCajita) {
        return cajitaRepositorio.buscarPorId(idUsuario, idCajita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cajita no encontrada"));
    }

    public List<Movimiento> movimientos(Long idUsuario, Long idCajita) {
        return movimientoRepositorio.listarPorCajita(idUsuario, idCajita, 50);
    }

    public BigDecimal disponible(Long idUsuario) {
        return movimientoRepositorio.disponible(idUsuario);
    }

    public CajitaForm formularioParaEditar(Long idUsuario, Long idCajita) {
        Cajita cajita = obtener(idUsuario, idCajita);
        CajitaForm f = new CajitaForm();
        f.setNombre(cajita.getNombre());
        f.setProposito(cajita.getProposito());
        f.setPorcentaje(cajita.getPorcentaje().stripTrailingZeros());
        f.setEsResto(cajita.isEsResto());
        return f;
    }

    // ------------------------------------------------------------------ administracion

    @Transactional
    public void crear(Long idUsuario, CajitaForm formulario) {
        String nombre = formulario.getNombre().trim();
        validarNombreUnico(idUsuario, nombre, null);
        BigDecimal porcentaje = formulario.isEsResto() ? BigDecimal.ZERO : formulario.getPorcentaje();
        validarSumaPorcentajes(idUsuario, null, porcentaje);
        BigDecimal saldoInicial = Dinero.esVacioOCero(formulario.getSaldoInicial()) ? null
                : Dinero.parsear(formulario.getSaldoInicial(), "saldoInicial");

        if (formulario.isEsResto()) {
            cajitaRepositorio.desmarcarResto(idUsuario, null);
        }
        Cajita cajita = new Cajita();
        cajita.setIdUsuario(idUsuario);
        cajita.setNombre(nombre);
        cajita.setProposito(formulario.getProposito());
        cajita.setPorcentaje(porcentaje);
        cajita.setEsResto(formulario.isEsResto());
        Long idCajita = cajitaRepositorio.insertar(cajita);

        if (saldoInicial != null) {
            insertarOperacion(idUsuario, idCajita, TipoMovimiento.SALDO_INICIAL, saldoInicial, hoy(),
                    "Saldo que ya tenía la cajita");
        }
    }

    @Transactional
    public void actualizar(Long idUsuario, Long idCajita, CajitaForm formulario) {
        Cajita cajita = obtener(idUsuario, idCajita);
        String nombre = formulario.getNombre().trim();
        validarNombreUnico(idUsuario, nombre, idCajita);
        BigDecimal porcentaje = formulario.isEsResto() ? BigDecimal.ZERO : formulario.getPorcentaje();
        if (cajita.isActiva()) {
            validarSumaPorcentajes(idUsuario, idCajita, porcentaje);
        }
        if (formulario.isEsResto()) {
            cajitaRepositorio.desmarcarResto(idUsuario, idCajita);
        }
        cajita.setNombre(nombre);
        cajita.setProposito(formulario.getProposito());
        cajita.setPorcentaje(porcentaje);
        cajita.setEsResto(formulario.isEsResto());
        cajitaRepositorio.actualizar(cajita);
    }

    @Transactional
    public void cambiarEstado(Long idUsuario, Long idCajita, boolean activa) {
        Cajita cajita = obtener(idUsuario, idCajita);
        if (!activa && cajita.getSaldo().signum() != 0) {
            throw new ReglaNegocioException("Para desactivar " + cajita.getNombre()
                    + " primero retira su saldo (" + Dinero.formatear(cajita.getSaldo()) + ").");
        }
        if (activa) {
            validarSumaPorcentajes(idUsuario, idCajita, cajita.getPorcentaje());
            if (cajita.isEsResto()) {
                cajitaRepositorio.desmarcarResto(idUsuario, idCajita);
            }
        }
        cajitaRepositorio.cambiarEstado(idUsuario, idCajita, activa);
    }

    // ------------------------------------------------------------------ operaciones

    @Transactional
    public void registrarOperacion(Long idUsuario, Long idCajita, OperacionCajitaForm formulario) {
        Cajita cajita = obtener(idUsuario, idCajita);
        if (!cajita.isActiva()) {
            throw new ReglaNegocioException("La cajita está desactivada.");
        }
        TipoMovimiento operacion = formulario.getOperacion();
        if (operacion == null || !operacion.esOperacionDeCajita()) {
            throw new ReglaNegocioException("operacion", "Elige una operación válida.");
        }
        LocalDate fecha = formulario.getFecha();
        if (fecha == null || fecha.isAfter(hoy())) {
            throw new ReglaNegocioException("fecha", "La fecha no puede ser futura.");
        }
        BigDecimal monto = Dinero.parsear(formulario.getMonto(), "monto");

        if (operacion == TipoMovimiento.APORTE) {
            BigDecimal disponible = disponible(idUsuario);
            if (disponible.compareTo(monto) < 0) {
                throw new ReglaNegocioException("monto", "Solo tienes " + Dinero.formatear(disponible)
                        + " disponible por repartir. Si ese dinero ya estaba en la cajita antes de usar Eden, "
                        + "regístralo como \"Saldo que ya tenía\".");
            }
        } else if (operacion == TipoMovimiento.RETIRO && cajita.getSaldo().compareTo(monto) < 0) {
            throw new ReglaNegocioException("monto", cajita.getNombre() + " solo tiene "
                    + Dinero.formatear(cajita.getSaldo()) + ".");
        }

        String descripcion = formulario.getDescripcion() == null || formulario.getDescripcion().isBlank()
                ? null : formulario.getDescripcion().trim();
        insertarOperacion(idUsuario, idCajita, operacion, monto, fecha, descripcion);
    }

    // ------------------------------------------------------------------ reparto (RN-03, RN-04)

    /** @param montoTexto monto a repartir; vacio = todo el disponible. */
    public PropuestaReparto proponerReparto(Long idUsuario, String montoTexto) {
        BigDecimal disponible = disponible(idUsuario);
        BigDecimal monto = Dinero.esVacioOCero(montoTexto)
                ? disponible.max(BigDecimal.ZERO)
                : Dinero.parsear(montoTexto, "monto");
        if (monto.compareTo(disponible.max(BigDecimal.ZERO)) > 0) {
            throw new ReglaNegocioException("monto", "No puedes repartir más de lo disponible ("
                    + Dinero.formatear(disponible) + ").");
        }
        List<Cajita> activas = listarActivas(idUsuario);
        BigDecimal suma = activas.stream().filter(c -> !c.isEsResto())
                .map(Cajita::getPorcentaje).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hayResto = activas.stream().anyMatch(Cajita::isEsResto);
        return new PropuestaReparto(disponible, CalculadoraReparto.proponer(monto, activas), suma, hayResto);
    }

    /** Crea todos los aportes del reparto en una sola transaccion: o todos o ninguno. */
    @Transactional
    public BigDecimal confirmarReparto(Long idUsuario, RepartoForm formulario) {
        Map<Long, Cajita> activas = listarActivas(idUsuario).stream()
                .collect(Collectors.toMap(Cajita::getId, Function.identity()));
        LocalDate fecha = hoy();
        BigDecimal total = BigDecimal.ZERO;

        record Aporte(Cajita cajita, BigDecimal monto) { }
        List<Aporte> aportes = new java.util.ArrayList<>();

        for (RepartoForm.Linea linea : formulario.getLineas()) {
            if (Dinero.esVacioOCero(linea.getMonto())) {
                continue;
            }
            Cajita cajita = activas.get(linea.getIdCajita());
            if (cajita == null) {
                throw new ReglaNegocioException("Una de las cajitas del reparto ya no está activa. Vuelve a cargar el reparto.");
            }
            BigDecimal monto;
            try {
                monto = Dinero.parsear(linea.getMonto(), "monto");
            } catch (ReglaNegocioException e) {
                throw new ReglaNegocioException("Revisa el monto de " + cajita.getNombre() + ": " + e.getMessage());
            }
            aportes.add(new Aporte(cajita, monto));
            total = total.add(monto);
        }

        if (aportes.isEmpty()) {
            throw new ReglaNegocioException("Asigna un monto al menos a una cajita.");
        }
        BigDecimal disponible = disponible(idUsuario);
        if (total.compareTo(disponible) > 0) {
            throw new ReglaNegocioException("El reparto suma " + Dinero.formatear(total)
                    + " y solo tienes " + Dinero.formatear(disponible) + " disponible.");
        }
        for (Aporte aporte : aportes) {
            insertarOperacion(idUsuario, aporte.cajita().getId(), TipoMovimiento.APORTE, aporte.monto(), fecha, "Reparto");
        }
        return total;
    }

    // ------------------------------------------------------------------ utilidades

    private void validarSumaPorcentajes(Long idUsuario, Long idExcluido, BigDecimal porcentajeNuevo) {
        BigDecimal suma = listarActivas(idUsuario).stream()
                .filter(c -> !c.isEsResto() && !c.getId().equals(idExcluido))
                .map(Cajita::getPorcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(porcentajeNuevo);
        if (suma.compareTo(CIEN) > 0) {
            throw new ReglaNegocioException("porcentaje", "Con este porcentaje tus cajitas sumarían "
                    + suma.stripTrailingZeros().toPlainString() + "%. El máximo es 100%.");
        }
    }

    private void validarNombreUnico(Long idUsuario, String nombre, Long idExcluido) {
        if (cajitaRepositorio.existeNombre(idUsuario, nombre, idExcluido)) {
            throw new ReglaNegocioException("nombre", "Ya tienes una cajita con ese nombre.");
        }
    }

    private void insertarOperacion(Long idUsuario, Long idCajita, TipoMovimiento tipo, BigDecimal monto,
                                   LocalDate fecha, String descripcion) {
        Movimiento m = new Movimiento();
        m.setIdUsuario(idUsuario);
        m.setTipo(tipo);
        m.setFecha(fecha);
        m.setMonto(monto);
        m.setIdCajita(idCajita);
        m.setDescripcion(descripcion);
        movimientoRepositorio.insertar(m);
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }
}
