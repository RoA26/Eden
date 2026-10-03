package com.eden.web;

import com.eden.dto.FiltroMovimientos;
import com.eden.modelo.TipoMovimiento;
import com.eden.servicio.CalendarioService;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.FuenteIngresoService;
import com.eden.servicio.MovimientoService;
import com.eden.servicio.TableroService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Prepara el modelo de las vistas de consulta (tablero, movimientos y
 * calendario). Lo comparten la cuenta propia y las cuentas compartidas de
 * solo lectura, para no duplicar la logica de presentacion.
 */
@Component
class VistasCuenta {

    static final String VISTA_TABLERO = "inicio";
    static final String VISTA_MOVIMIENTOS = "movimientos/lista";
    static final String VISTA_CALENDARIO = "calendario";

    private final TableroService tableroService;
    private final MovimientoService movimientoService;
    private final CategoriaService categoriaService;
    private final FuenteIngresoService fuenteService;
    private final CalendarioService calendarioService;

    VistasCuenta(TableroService tableroService, MovimientoService movimientoService, CategoriaService categoriaService,
                 FuenteIngresoService fuenteService, CalendarioService calendarioService) {
        this.tableroService = tableroService;
        this.movimientoService = movimientoService;
        this.categoriaService = categoriaService;
        this.fuenteService = fuenteService;
        this.calendarioService = calendarioService;
    }

    String tablero(Long idUsuario, Model model) {
        model.addAttribute("tablero", tableroService.construir(idUsuario));
        return VISTA_TABLERO;
    }

    String movimientos(Long idUsuario, FiltroMovimientos filtro, Model model) {
        model.addAttribute("pagina", movimientoService.buscar(idUsuario, filtro));
        model.addAttribute("categorias", categoriaService.listar(idUsuario));
        model.addAttribute("fuentes", fuenteService.listar(idUsuario));
        model.addAttribute("tipos", TipoMovimiento.values());
        return VISTA_MOVIMIENTOS;
    }

    /** @param mes en formato AAAA-MM; si falta o es invalido se usa el mes actual. */
    String calendario(Long idUsuario, String mes, Model model) {
        YearMonth actual = calendarioService.mesActual();
        YearMonth solicitado;
        try {
            solicitado = mes == null || mes.isBlank() ? actual : YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            solicitado = actual;
        }
        model.addAttribute("calendario", calendarioService.construir(idUsuario, solicitado));
        model.addAttribute("esMesActual", solicitado.equals(actual));
        return VISTA_CALENDARIO;
    }
}
