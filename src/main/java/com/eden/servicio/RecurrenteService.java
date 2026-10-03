package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.ConfirmacionRecurrenteForm;
import com.eden.dto.MovimientoForm;
import com.eden.dto.RecurrenteForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Cajita;
import com.eden.modelo.Dinero;
import com.eden.modelo.Movimiento;
import com.eden.modelo.Recurrente;
import com.eden.modelo.TipoMovimiento;
import com.eden.persistencia.RecurrenteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Movimientos recurrentes: Eden los recuerda cuando se acerca su fecha y
 * el usuario los confirma (con el monto real) u omite. Nunca se registran
 * solos. El registro pasa por {@link MovimientoService}, asi que aplican
 * las mismas reglas que cualquier ingreso o gasto.
 */
@Service
public class RecurrenteService {

    private final RecurrenteRepositorio recurrenteRepositorio;
    private final MovimientoService movimientoService;
    private final CajitaService cajitaService;
    private final Clock reloj;

    public RecurrenteService(RecurrenteRepositorio recurrenteRepositorio, MovimientoService movimientoService,
                             CajitaService cajitaService, Clock reloj) {
        this.recurrenteRepositorio = recurrenteRepositorio;
        this.movimientoService = movimientoService;
        this.cajitaService = cajitaService;
        this.reloj = reloj;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }

    public List<Recurrente> listar(Long idUsuario) {
        return recurrenteRepositorio.listar(idUsuario);
    }

    public List<Recurrente> pendientes(Long idUsuario) {
        return recurrenteRepositorio.listarPendientes(idUsuario, hoy());
    }

    public Recurrente obtener(Long idUsuario, Long idRecurrente) {
        return recurrenteRepositorio.buscarPorId(idUsuario, idRecurrente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recurrente no encontrado"));
    }

    public RecurrenteForm formularioParaEditar(Long idUsuario, Long idRecurrente) {
        Recurrente r = obtener(idUsuario, idRecurrente);
        RecurrenteForm f = new RecurrenteForm();
        f.setNombre(r.getNombre());
        f.setTipo(r.getTipo());
        f.setMonto(r.getMonto().stripTrailingZeros().toPlainString());
        f.setIdCategoria(r.getIdCategoria());
        f.setIdFuente(r.getIdFuente());
        f.setIdCajitaOrigen(r.getIdCajitaOrigen());
        f.setPeriodicidad(r.getPeriodicidad());
        f.setProximaFecha(r.getProximaFecha());
        f.setDiasAviso(r.getDiasAviso());
        return f;
    }

    @Transactional
    public void crear(Long idUsuario, RecurrenteForm formulario) {
        recurrenteRepositorio.insertar(construir(idUsuario, formulario, null));
    }

    @Transactional
    public void actualizar(Long idUsuario, Long idRecurrente, RecurrenteForm formulario) {
        Recurrente existente = obtener(idUsuario, idRecurrente);
        Recurrente actualizado = construir(idUsuario, formulario, idRecurrente);
        actualizado.setId(existente.getId());
        recurrenteRepositorio.actualizar(actualizado);
    }

    @Transactional
    public void cambiarEstado(Long idUsuario, Long idRecurrente, boolean activo) {
        obtener(idUsuario, idRecurrente);
        recurrenteRepositorio.cambiarEstado(idUsuario, idRecurrente, activo);
    }

    /** Los movimientos ya registrados se conservan: no dependen del recurrente. */
    @Transactional
    public void eliminar(Long idUsuario, Long idRecurrente) {
        obtener(idUsuario, idRecurrente);
        recurrenteRepositorio.eliminar(idUsuario, idRecurrente);
    }

    /**
     * Registra el movimiento del periodo y programa el siguiente. Todo en una
     * transaccion: si el registro falla, la fecha no avanza.
     */
    @Transactional
    public Recurrente confirmar(Long idUsuario, Long idRecurrente, ConfirmacionRecurrenteForm confirmacion) {
        Recurrente r = obtener(idUsuario, idRecurrente);
        if (!r.isActivo()) {
            throw new ReglaNegocioException("Este recurrente está pausado.");
        }
        LocalDate hoy = hoy();
        LocalDate fecha = confirmacion.getFecha() != null ? confirmacion.getFecha()
                : (r.getProximaFecha().isAfter(hoy) ? hoy : r.getProximaFecha());

        MovimientoForm movimiento = new MovimientoForm();
        movimiento.setTipo(r.getTipo());
        movimiento.setFecha(fecha);
        movimiento.setMonto(Dinero.esVacioOCero(confirmacion.getMonto())
                ? r.getMonto().toPlainString() : confirmacion.getMonto());
        movimiento.setIdCategoria(r.getIdCategoria());
        movimiento.setIdFuente(r.getIdFuente());
        movimiento.setIdCajitaOrigen(r.getIdCajitaOrigen());
        movimiento.setDescripcion(r.getNombre());
        movimientoService.registrar(idUsuario, movimiento);

        r.avanzar();
        recurrenteRepositorio.actualizarProximaFecha(idUsuario, idRecurrente, r.getProximaFecha());
        return r;
    }

    /** Salta este periodo sin registrar nada (ej. este mes no se pagó). */
    @Transactional
    public Recurrente omitir(Long idUsuario, Long idRecurrente) {
        Recurrente r = obtener(idUsuario, idRecurrente);
        r.avanzar();
        recurrenteRepositorio.actualizarProximaFecha(idUsuario, idRecurrente, r.getProximaFecha());
        return r;
    }

    private Recurrente construir(Long idUsuario, RecurrenteForm f, Long idExcluido) {
        String nombre = f.getNombre().trim();
        if (recurrenteRepositorio.existeNombre(idUsuario, nombre, idExcluido)) {
            throw new ReglaNegocioException("nombre", "Ya tienes un recurrente con ese nombre.");
        }
        if (f.getProximaFecha() == null) {
            throw new ReglaNegocioException("proximaFecha", "Elige la próxima fecha.");
        }

        // Mismas reglas de un ingreso o gasto normal (tipo, monto, categoria, fuente).
        MovimientoForm prueba = new MovimientoForm();
        prueba.setTipo(f.getTipo());
        prueba.setFecha(hoy());
        prueba.setMonto(f.getMonto());
        prueba.setIdCategoria(f.getIdCategoria());
        prueba.setIdFuente(f.getIdFuente());
        Movimiento valido = movimientoService.validar(idUsuario, prueba);

        Long idCajita = f.getTipo() == TipoMovimiento.GASTO ? f.getIdCajitaOrigen() : null;
        if (idCajita != null) {
            Cajita cajita;
            try {
                cajita = cajitaService.obtener(idUsuario, idCajita);
            } catch (RecursoNoEncontradoException e) {
                throw new ReglaNegocioException("idCajitaOrigen", "Esa cajita no existe.");
            }
            if (!cajita.isActiva()) {
                throw new ReglaNegocioException("idCajitaOrigen", "Esa cajita está desactivada.");
            }
        }

        Recurrente r = new Recurrente();
        r.setIdUsuario(idUsuario);
        r.setNombre(nombre);
        r.setTipo(valido.getTipo());
        r.setMonto(valido.getMonto());
        r.setIdCategoria(valido.getIdCategoria());
        r.setIdFuente(valido.getIdFuente());
        r.setIdCajitaOrigen(idCajita);
        r.setPeriodicidad(f.getPeriodicidad());
        r.setDiaAncla(f.getProximaFecha().getDayOfMonth());
        r.setProximaFecha(f.getProximaFecha());
        r.setDiasAviso(f.getDiasAviso());
        return r;
    }
}
