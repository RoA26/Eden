package com.eden.servicio;

import com.eden.dto.FuenteForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.FuenteIngreso;
import com.eden.persistencia.FuenteIngresoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuenteIngresoService {

    private final FuenteIngresoRepositorio fuenteRepositorio;

    public FuenteIngresoService(FuenteIngresoRepositorio fuenteRepositorio) {
        this.fuenteRepositorio = fuenteRepositorio;
    }

    public List<FuenteIngreso> listar(Long idUsuario) {
        return fuenteRepositorio.listar(idUsuario);
    }

    public List<FuenteIngreso> listarActivas(Long idUsuario) {
        return fuenteRepositorio.listar(idUsuario).stream().filter(FuenteIngreso::isActiva).toList();
    }

    public FuenteIngreso obtener(Long idUsuario, Long idFuente) {
        return fuenteRepositorio.buscarPorId(idUsuario, idFuente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Fuente no encontrada"));
    }

    public FuenteIngreso obtenerActiva(Long idUsuario, Long idFuente) {
        FuenteIngreso fuente = obtener(idUsuario, idFuente);
        if (!fuente.isActiva()) {
            throw new ReglaNegocioException("idFuente", "Esa fuente está desactivada. Actívala para registrarle movimientos.");
        }
        return fuente;
    }

    @Transactional
    public void crear(Long idUsuario, FuenteForm formulario) {
        String nombre = formulario.getNombre().trim();
        validarNombreUnico(idUsuario, nombre, null);
        FuenteIngreso fuente = new FuenteIngreso();
        fuente.setIdUsuario(idUsuario);
        fuente.setNombre(nombre);
        fuente.setFrecuencia(formulario.getFrecuencia());
        fuenteRepositorio.insertar(fuente);
    }

    @Transactional
    public void actualizar(Long idUsuario, Long idFuente, FuenteForm formulario) {
        FuenteIngreso fuente = obtener(idUsuario, idFuente);
        String nombre = formulario.getNombre().trim();
        validarNombreUnico(idUsuario, nombre, idFuente);
        fuente.setNombre(nombre);
        fuente.setFrecuencia(formulario.getFrecuencia());
        fuenteRepositorio.actualizar(fuente);
    }

    @Transactional
    public void cambiarEstado(Long idUsuario, Long idFuente, boolean activa) {
        obtener(idUsuario, idFuente);
        fuenteRepositorio.cambiarEstado(idUsuario, idFuente, activa);
    }

    private void validarNombreUnico(Long idUsuario, String nombre, Long idExcluido) {
        if (fuenteRepositorio.existeNombre(idUsuario, nombre, idExcluido)) {
            throw new ReglaNegocioException("nombre", "Ya tienes una fuente con ese nombre.");
        }
    }
}
