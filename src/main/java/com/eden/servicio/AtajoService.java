package com.eden.servicio;

import com.eden.dto.AtajoForm;
import com.eden.dto.MovimientoForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Atajo;
import com.eden.modelo.Movimiento;
import com.eden.persistencia.AtajoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Botones rapidos del tablero ("friccion cero"): plantillas de ingreso o
 * gasto que se registran con un toque, con la fecha de hoy. Se validan con
 * las mismas reglas del formulario general al crearlos y otra vez al usarlos
 * (la categoria o la fuente pudieron desactivarse despues).
 */
@Service
public class AtajoService {

    static final int MAXIMO_POR_USUARIO = 12;

    private final AtajoRepositorio atajoRepositorio;
    private final MovimientoService movimientoService;

    public AtajoService(AtajoRepositorio atajoRepositorio, MovimientoService movimientoService) {
        this.atajoRepositorio = atajoRepositorio;
        this.movimientoService = movimientoService;
    }

    public List<Atajo> listar(Long idUsuario) {
        return atajoRepositorio.listar(idUsuario);
    }

    public Atajo obtener(Long idUsuario, Long idAtajo) {
        return atajoRepositorio.buscarPorId(idUsuario, idAtajo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Botón rápido no encontrado"));
    }

    @Transactional
    public Atajo crear(Long idUsuario, AtajoForm formulario) {
        String nombre = formulario.getNombre() == null ? "" : formulario.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("nombre", "Escribe el nombre del botón.");
        }
        if (atajoRepositorio.contar(idUsuario) >= MAXIMO_POR_USUARIO) {
            throw new ReglaNegocioException("Puedes tener hasta " + MAXIMO_POR_USUARIO
                    + " botones rápidos. Elimina uno para crear otro.");
        }
        if (atajoRepositorio.existeNombre(idUsuario, nombre)) {
            throw new ReglaNegocioException("nombre", "Ya tienes un botón con ese nombre.");
        }

        Movimiento plantilla = movimientoService.validar(idUsuario, comoMovimiento(formulario));

        Atajo atajo = new Atajo();
        atajo.setIdUsuario(idUsuario);
        atajo.setNombre(nombre);
        atajo.setTipo(plantilla.getTipo());
        atajo.setMonto(plantilla.getMonto());
        atajo.setIdCategoria(plantilla.getIdCategoria());
        atajo.setIdFuente(plantilla.getIdFuente());
        atajo.setId(atajoRepositorio.insertar(atajo));
        return atajo;
    }

    @Transactional
    public void eliminar(Long idUsuario, Long idAtajo) {
        obtener(idUsuario, idAtajo);
        atajoRepositorio.eliminar(idUsuario, idAtajo);
    }

    /** Registra el movimiento del boton con la fecha de hoy y devuelve su id. */
    @Transactional
    public Long usar(Long idUsuario, Long idAtajo) {
        Atajo atajo = obtener(idUsuario, idAtajo);
        MovimientoForm movimiento = new MovimientoForm();
        movimiento.setTipo(atajo.getTipo());
        movimiento.setFecha(movimientoService.hoy());
        movimiento.setMonto(atajo.getMonto().toPlainString());
        movimiento.setIdCategoria(atajo.getIdCategoria());
        movimiento.setIdFuente(atajo.getIdFuente());
        movimiento.setDescripcion(atajo.getNombre());
        return movimientoService.registrar(idUsuario, movimiento);
    }

    private MovimientoForm comoMovimiento(AtajoForm formulario) {
        MovimientoForm movimiento = new MovimientoForm();
        movimiento.setTipo(formulario.getTipo());
        movimiento.setFecha(movimientoService.hoy());
        movimiento.setMonto(formulario.getMonto());
        movimiento.setIdCategoria(formulario.getIdCategoria());
        movimiento.setIdFuente(formulario.getIdFuente());
        return movimiento;
    }
}
