package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.MetaForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Cajita;
import com.eden.modelo.Dinero;
import com.eden.modelo.Meta;
import com.eden.modelo.ProgresoMeta;
import com.eden.persistencia.MetaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Metas de ahorro: el progreso sale del saldo de la cajita vinculada. */
@Service
public class MetaService {

    private final MetaRepositorio metaRepositorio;
    private final CajitaService cajitaService;
    private final Clock reloj;

    public MetaService(MetaRepositorio metaRepositorio, CajitaService cajitaService, Clock reloj) {
        this.metaRepositorio = metaRepositorio;
        this.cajitaService = cajitaService;
        this.reloj = reloj;
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }

    public List<ProgresoMeta> listar(Long idUsuario) {
        LocalDate hoy = hoy();
        return metaRepositorio.listar(idUsuario).stream().map(m -> ProgresoMeta.calcular(m, hoy)).toList();
    }

    public List<ProgresoMeta> listarActivas(Long idUsuario) {
        return listar(idUsuario).stream().filter(p -> p.meta().isActiva()).toList();
    }

    public Meta obtener(Long idUsuario, Long idMeta) {
        return metaRepositorio.buscarPorId(idUsuario, idMeta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Meta no encontrada"));
    }

    public MetaForm formularioParaEditar(Long idUsuario, Long idMeta) {
        Meta meta = obtener(idUsuario, idMeta);
        MetaForm f = new MetaForm();
        f.setNombre(meta.getNombre());
        f.setMontoObjetivo(meta.getMontoObjetivo().stripTrailingZeros().toPlainString());
        f.setFechaObjetivo(meta.getFechaObjetivo());
        f.setIdCajita(meta.getIdCajita());
        return f;
    }

    @Transactional
    public void crear(Long idUsuario, MetaForm formulario) {
        metaRepositorio.insertar(construir(idUsuario, formulario, null));
    }

    @Transactional
    public void actualizar(Long idUsuario, Long idMeta, MetaForm formulario) {
        obtener(idUsuario, idMeta);
        Meta meta = construir(idUsuario, formulario, idMeta);
        meta.setId(idMeta);
        metaRepositorio.actualizar(meta);
    }

    @Transactional
    public void cambiarEstado(Long idUsuario, Long idMeta, boolean activa) {
        obtener(idUsuario, idMeta);
        metaRepositorio.cambiarEstado(idUsuario, idMeta, activa);
    }

    /** Eliminar una meta no toca el dinero: sigue en su cajita. */
    @Transactional
    public void eliminar(Long idUsuario, Long idMeta) {
        obtener(idUsuario, idMeta);
        metaRepositorio.eliminar(idUsuario, idMeta);
    }

    private Meta construir(Long idUsuario, MetaForm f, Long idExcluido) {
        String nombre = f.getNombre().trim();
        if (metaRepositorio.existeNombre(idUsuario, nombre, idExcluido)) {
            throw new ReglaNegocioException("nombre", "Ya tienes una meta con ese nombre.");
        }
        if (f.getFechaObjetivo() != null && !f.getFechaObjetivo().isAfter(hoy()) && idExcluido == null) {
            throw new ReglaNegocioException("fechaObjetivo", "La fecha objetivo debe ser futura.");
        }
        Cajita cajita;
        try {
            cajita = cajitaService.obtener(idUsuario, f.getIdCajita());
        } catch (RecursoNoEncontradoException e) {
            throw new ReglaNegocioException("idCajita", "Esa cajita no existe.");
        }
        if (!cajita.isActiva()) {
            throw new ReglaNegocioException("idCajita", "Esa cajita está desactivada.");
        }

        Meta meta = new Meta();
        meta.setIdUsuario(idUsuario);
        meta.setNombre(nombre);
        meta.setMontoObjetivo(Dinero.parsear(f.getMontoObjetivo(), "montoObjetivo"));
        meta.setFechaObjetivo(f.getFechaObjetivo());
        meta.setIdCajita(cajita.getId());
        return meta;
    }
}
