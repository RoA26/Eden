package com.eden.web;

import com.eden.modelo.TipoCategoria;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AccesoCompartidoService;
import com.eden.servicio.AtajoService;
import com.eden.servicio.CajitaService;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.FuenteIngresoService;
import com.eden.servicio.MovimientoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@Controller
public class InicioController {

    /** Secciones del tablero que el navegador puede volver a pedir tras una accion sin recarga. */
    private static final Set<String> SECCIONES_PARCIALES = Set.of("atajos", "resumenMes", "ultimos", "cajitas");

    private final VistasCuenta vistas;
    private final AccesoCompartidoService accesoService;
    private final AtajoService atajoService;
    private final CategoriaService categoriaService;
    private final FuenteIngresoService fuenteService;
    private final CajitaService cajitaService;
    private final MovimientoService movimientoService;

    public InicioController(VistasCuenta vistas, AccesoCompartidoService accesoService, AtajoService atajoService,
                            CategoriaService categoriaService, FuenteIngresoService fuenteService,
                            CajitaService cajitaService, MovimientoService movimientoService) {
        this.vistas = vistas;
        this.accesoService = accesoService;
        this.atajoService = atajoService;
        this.categoriaService = categoriaService;
        this.fuenteService = fuenteService;
        this.cajitaService = cajitaService;
        this.movimientoService = movimientoService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    @GetMapping("/inicio")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        prepararTablero(usuario, model);
        return vistas.tablero(usuario.getId(), model);
    }

    /** Devuelve solo una seccion del tablero (fragmento Thymeleaf) para refrescarla en el DOM. */
    @GetMapping("/inicio/parcial/{seccion}")
    public String parcial(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable String seccion, Model model) {
        if (!SECCIONES_PARCIALES.contains(seccion)) {
            return "redirect:/inicio";
        }
        prepararTablero(usuario, model);
        return vistas.tablero(usuario.getId(), model) + " :: " + seccion;
    }

    @GetMapping("/mas")
    public String mas(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("cuentasCompartidas", accesoService.recibidos(usuario.getId()));
        return "mas";
    }

    /** Botones rapidos y listas que necesitan los modales del tablero (solo en la cuenta propia). */
    private void prepararTablero(UsuarioAutenticado usuario, Model model) {
        Long idUsuario = usuario.getId();
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        model.addAttribute("atajos", atajoService.listar(idUsuario));
        model.addAttribute("categoriasIngreso", categoriaService.listarActivas(idUsuario, TipoCategoria.INGRESO));
        model.addAttribute("categoriasGasto", categoriaService.listarActivas(idUsuario, TipoCategoria.GASTO));
        model.addAttribute("categoriasCosto", categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO));
        model.addAttribute("fuentes", fuenteService.listarActivas(idUsuario));
        model.addAttribute("cajitasActivas", cajitaService.listarActivas(idUsuario));
        model.addAttribute("tiposCategoria", TipoCategoria.values());
        model.addAttribute("hoy", movimientoService.hoy());
    }
}
