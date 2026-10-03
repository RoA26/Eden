package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.FiltroMovimientos;
import com.eden.dto.MovimientoForm;
import com.eden.dto.PaginaMovimientos;
import com.eden.dto.ProducidoForm;
import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Cajita;
import com.eden.modelo.Categoria;
import com.eden.modelo.Dinero;
import com.eden.modelo.Movimiento;
import com.eden.modelo.TipoCategoria;
import com.eden.modelo.TipoMovimiento;
import com.eden.persistencia.CajitaRepositorio;
import com.eden.persistencia.MovimientoRepositorio;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;

/**
 * Reglas de negocio de ingresos y gastos: registro rapido del producido
 * (RN-01), coherencia entre tipo, categoria y fuente, y gastos pagados
 * desde una cajita (RN-06).
 */
@Service
public class MovimientoService {

    private static final int TAMANO_PAGINA = 30;
    private static final String MENSAJE_PRODUCIDO_DUPLICADO =
            "Ya registraste el producido de esa fuente para ese día. Si quieres cambiarlo, edítalo desde Movimientos.";

    private final MovimientoRepositorio movimientoRepositorio;
    private final CajitaRepositorio cajitaRepositorio;
    private final CategoriaService categoriaService;
    private final FuenteIngresoService fuenteService;
    private final Clock reloj;

    public MovimientoService(MovimientoRepositorio movimientoRepositorio, CajitaRepositorio cajitaRepositorio,
                             CategoriaService categoriaService, FuenteIngresoService fuenteService, Clock reloj) {
        this.movimientoRepositorio = movimientoRepositorio;
        this.cajitaRepositorio = cajitaRepositorio;
        this.categoriaService = categoriaService;
        this.fuenteService = fuenteService;
        this.reloj = reloj;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }

    // ------------------------------------------------------------------ consultas

    public Movimiento obtener(Long idUsuario, Long idMovimiento) {
        return movimientoRepositorio.buscarPorId(idUsuario, idMovimiento)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento no encontrado"));
    }

    /** Sin fechas en el filtro, muestra el mes actual. */
    public PaginaMovimientos buscar(Long idUsuario, FiltroMovimientos filtro) {
        if (filtro.getDesde() == null && filtro.getHasta() == null) {
            YearMonth mes = YearMonth.from(hoy());
            filtro.setDesde(mes.atDay(1));
            filtro.setHasta(mes.atEndOfMonth());
        }
        return movimientoRepositorio.buscar(idUsuario, filtro, TAMANO_PAGINA);
    }

    public MovimientoForm formularioParaEditar(Long idUsuario, Long idMovimiento) {
        Movimiento m = obtener(idUsuario, idMovimiento);
        if (!m.isEditable()) {
            throw new ReglaNegocioException("Este movimiento no se puede editar; elimínalo y regístralo de nuevo.");
        }
        MovimientoForm f = new MovimientoForm();
        f.setTipo(m.getTipo());
        f.setFecha(m.getFecha());
        f.setMonto(m.getMonto().stripTrailingZeros().toPlainString());
        f.setIdCategoria(m.getIdCategoria());
        f.setIdFuente(m.getIdFuente());
        f.setDescripcion(m.getDescripcion());
        return f;
    }

    // ------------------------------------------------------------------ registro rapido (RN-01)

    @Transactional
    public void registrarProducido(Long idUsuario, ProducidoForm formulario) {
        fuenteService.obtenerActiva(idUsuario, formulario.getIdFuente());
        validarFecha(formulario.getFecha(), "fecha");
        Categoria categoriaIngreso = validarCategoria(idUsuario, formulario.getIdCategoriaIngreso(),
                "idCategoriaIngreso", null, TipoCategoria.INGRESO);
        BigDecimal monto = Dinero.parsear(formulario.getMonto(), "monto");
        BigDecimal costo = Dinero.esVacioOCero(formulario.getCosto()) ? null
                : Dinero.parsear(formulario.getCosto(), "costo");

        Movimiento ingreso = nuevo(idUsuario, TipoMovimiento.INGRESO, formulario.getFecha(), monto);
        ingreso.setIdCategoria(categoriaIngreso.getId());
        ingreso.setIdFuente(formulario.getIdFuente());
        insertarControlandoDuplicado(ingreso);

        if (costo != null) {
            if (formulario.getIdCategoriaCosto() == null) {
                throw new ReglaNegocioException("idCategoriaCosto", "Elige la categoría del costo.");
            }
            Categoria categoriaCosto = validarCategoria(idUsuario, formulario.getIdCategoriaCosto(),
                    "idCategoriaCosto", null, TipoCategoria.COSTO_OPERATIVO);
            Movimiento gasto = nuevo(idUsuario, TipoMovimiento.GASTO, formulario.getFecha(), costo);
            gasto.setIdCategoria(categoriaCosto.getId());
            gasto.setIdFuente(formulario.getIdFuente());
            movimientoRepositorio.insertar(gasto);
        }
    }

    // ------------------------------------------------------------------ formulario general

    @Transactional
    public Long registrar(Long idUsuario, MovimientoForm formulario) {
        Movimiento movimiento = construir(idUsuario, formulario, null);

        Cajita cajitaOrigen = null;
        if (movimiento.getTipo() == TipoMovimiento.GASTO && formulario.getIdCajitaOrigen() != null) {
            cajitaOrigen = cajitaRepositorio.buscarPorId(idUsuario, formulario.getIdCajitaOrigen())
                    .orElseThrow(() -> new ReglaNegocioException("idCajitaOrigen", "Esa cajita no existe."));
            if (!cajitaOrigen.isActiva()) {
                throw new ReglaNegocioException("idCajitaOrigen", "Esa cajita está desactivada.");
            }
            validarSaldoSuficiente(cajitaOrigen, cajitaOrigen.getSaldo(), movimiento.getMonto(), "idCajitaOrigen");
        }

        Long idMovimiento = insertarControlandoDuplicado(movimiento);

        if (cajitaOrigen != null) {
            Movimiento retiro = nuevo(idUsuario, TipoMovimiento.RETIRO, movimiento.getFecha(), movimiento.getMonto());
            retiro.setIdCajita(cajitaOrigen.getId());
            retiro.setIdRelacionado(idMovimiento);
            retiro.setDescripcion("Pago de gasto");
            movimientoRepositorio.insertar(retiro);
        }
        return idMovimiento;
    }

    @Transactional
    public void actualizar(Long idUsuario, Long idMovimiento, MovimientoForm formulario) {
        Movimiento existente = obtener(idUsuario, idMovimiento);
        if (!existente.isEditable()) {
            throw new ReglaNegocioException("Este movimiento no se puede editar; elimínalo y regístralo de nuevo.");
        }
        formulario.setTipo(existente.getTipo()); // el tipo no cambia al editar

        Movimiento actualizado = construir(idUsuario, formulario, existente.getIdCategoria());
        actualizado.setId(idMovimiento);

        Optional<Movimiento> retiroAsociado = movimientoRepositorio.buscarRelacionado(idUsuario, idMovimiento);
        if (retiroAsociado.isPresent()) {
            Movimiento retiro = retiroAsociado.get();
            Cajita cajita = cajitaRepositorio.buscarPorId(idUsuario, retiro.getIdCajita())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cajita no encontrada"));
            // El monto anterior vuelve a estar disponible en la cajita para la validacion.
            validarSaldoSuficiente(cajita, cajita.getSaldo().add(retiro.getMonto()), actualizado.getMonto(), "monto");
        }

        try {
            movimientoRepositorio.actualizar(actualizado);
        } catch (DuplicateKeyException e) {
            throw new ReglaNegocioException("fecha", MENSAJE_PRODUCIDO_DUPLICADO);
        }
        retiroAsociado.ifPresent(r ->
                movimientoRepositorio.actualizarRelacionado(idUsuario, idMovimiento, actualizado.getFecha(), actualizado.getMonto()));
    }

    @Transactional
    public void eliminar(Long idUsuario, Long idMovimiento) {
        Movimiento m = obtener(idUsuario, idMovimiento);
        if (!m.isEliminable()) {
            throw new ReglaNegocioException("Este retiro pagó un gasto: elimina el gasto y el retiro se eliminará con él.");
        }
        if (m.getTipo() == TipoMovimiento.APORTE || m.getTipo() == TipoMovimiento.RENDIMIENTO
                || m.getTipo() == TipoMovimiento.SALDO_INICIAL) {
            BigDecimal saldo = cajitaRepositorio.saldo(idUsuario, m.getIdCajita());
            if (saldo.subtract(m.getMonto()).signum() < 0) {
                throw new ReglaNegocioException("No se puede eliminar: la cajita " + m.getNombreCajita()
                        + " quedaría en negativo. Primero elimina los retiros posteriores.");
            }
        }
        movimientoRepositorio.eliminar(idUsuario, idMovimiento);
    }

    /**
     * Valida un ingreso o gasto sin guardarlo. Lo usan otros servicios (por
     * ejemplo, los recurrentes) para aplicar exactamente las mismas reglas.
     * Devuelve el movimiento normalizado (monto parseado, fuente depurada).
     */
    public Movimiento validar(Long idUsuario, MovimientoForm formulario) {
        return construir(idUsuario, formulario, null);
    }

    // ------------------------------------------------------------------ validaciones

    /**
     * Construye y valida un ingreso o gasto. {@code idCategoriaActual} permite
     * conservar, al editar, una categoria que ya fue desactivada.
     */
    private Movimiento construir(Long idUsuario, MovimientoForm f, Long idCategoriaActual) {
        if (f.getTipo() == null || !f.getTipo().esIngresoOGasto()) {
            throw new ReglaNegocioException("tipo", "Elige si es un ingreso o un gasto.");
        }
        validarFecha(f.getFecha(), "fecha");
        BigDecimal monto = Dinero.parsear(f.getMonto(), "monto");

        Categoria categoria = f.getTipo() == TipoMovimiento.INGRESO
                ? validarCategoria(idUsuario, f.getIdCategoria(), "idCategoria", idCategoriaActual, TipoCategoria.INGRESO)
                : validarCategoria(idUsuario, f.getIdCategoria(), "idCategoria", idCategoriaActual,
                        TipoCategoria.GASTO, TipoCategoria.COSTO_OPERATIVO);

        Long idFuente = f.getIdFuente();
        if (categoria.getTipo() == TipoCategoria.GASTO) {
            idFuente = null; // los gastos personales no pertenecen a una fuente
        } else if (categoria.getTipo() == TipoCategoria.COSTO_OPERATIVO && idFuente == null) {
            throw new ReglaNegocioException("idFuente", "Los costos operativos deben asociarse a una fuente (por ejemplo, el motocarro).");
        }
        if (idFuente != null) {
            fuenteService.obtener(idUsuario, idFuente);
        }

        Movimiento m = nuevo(idUsuario, f.getTipo(), f.getFecha(), monto);
        m.setIdCategoria(categoria.getId());
        m.setIdFuente(idFuente);
        m.setDescripcion(f.getDescripcion() == null || f.getDescripcion().isBlank() ? null : f.getDescripcion().trim());
        return m;
    }

    private Categoria validarCategoria(Long idUsuario, Long idCategoria, String campo,
                                       Long idCategoriaActual, TipoCategoria... tiposPermitidos) {
        if (idCategoria == null) {
            throw new ReglaNegocioException(campo, "Elige una categoría.");
        }
        Categoria categoria;
        try {
            categoria = categoriaService.obtener(idUsuario, idCategoria);
        } catch (RecursoNoEncontradoException e) {
            throw new ReglaNegocioException(campo, "Esa categoría no existe.");
        }
        boolean tipoValido = false;
        for (TipoCategoria tipo : tiposPermitidos) {
            tipoValido |= categoria.getTipo() == tipo;
        }
        if (!tipoValido) {
            throw new ReglaNegocioException(campo, "Esa categoría no corresponde a este tipo de movimiento.");
        }
        if (!categoria.isActiva() && !Objects.equals(idCategoria, idCategoriaActual)) {
            throw new ReglaNegocioException(campo, "Esa categoría está desactivada.");
        }
        return categoria;
    }

    private void validarFecha(LocalDate fecha, String campo) {
        if (fecha == null) {
            throw new ReglaNegocioException(campo, "Elige la fecha.");
        }
        if (fecha.isAfter(hoy())) {
            throw new ReglaNegocioException(campo, "La fecha no puede ser futura.");
        }
    }

    private void validarSaldoSuficiente(Cajita cajita, BigDecimal saldoDisponible, BigDecimal monto, String campo) {
        if (saldoDisponible.compareTo(monto) < 0) {
            throw new ReglaNegocioException(campo, cajita.getNombre() + " solo tiene "
                    + Dinero.formatear(saldoDisponible) + ".");
        }
    }

    private Long insertarControlandoDuplicado(Movimiento movimiento) {
        try {
            return movimientoRepositorio.insertar(movimiento);
        } catch (DuplicateKeyException e) {
            throw new ReglaNegocioException("fecha", MENSAJE_PRODUCIDO_DUPLICADO);
        }
    }

    private static Movimiento nuevo(Long idUsuario, TipoMovimiento tipo, LocalDate fecha, BigDecimal monto) {
        Movimiento m = new Movimiento();
        m.setIdUsuario(idUsuario);
        m.setTipo(tipo);
        m.setFecha(fecha);
        m.setMonto(monto);
        return m;
    }
}
