package com.eden.web;

import com.eden.dto.FiltroMovimientos;
import com.eden.dto.MovimientoForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.TipoCategoria;
import com.eden.modelo.TipoMovimiento;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CajitaService;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.FuenteIngresoService;
import com.eden.servicio.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    private static final String VISTA_FORMULARIO = "movimientos/formulario";

    private final MovimientoService movimientoService;
    private final CategoriaService categoriaService;
    private final FuenteIngresoService fuenteService;
    private final CajitaService cajitaService;
    private final VistasCuenta vistas;

    public MovimientoController(MovimientoService movimientoService, CategoriaService categoriaService,
                                FuenteIngresoService fuenteService, CajitaService cajitaService, VistasCuenta vistas) {
        this.movimientoService = movimientoService;
        this.categoriaService = categoriaService;
        this.fuenteService = fuenteService;
        this.cajitaService = cajitaService;
        this.vistas = vistas;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                         @ModelAttribute("filtro") FiltroMovimientos filtro, Model model) {
        return vistas.movimientos(usuario.getId(), filtro, model);
    }

    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @RequestParam(defaultValue = "GASTO") TipoMovimiento tipo, Model model) {
        MovimientoForm formulario = new MovimientoForm();
        formulario.setTipo(tipo.esIngresoOGasto() ? tipo : TipoMovimiento.GASTO);
        formulario.setFecha(movimientoService.hoy());
        model.addAttribute("formulario", formulario);
        cargarListas(usuario.getId(), model, null);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                            @Valid @ModelAttribute("formulario") MovimientoForm formulario,
                            BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                movimientoService.registrar(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje",
                        formulario.getTipo() == TipoMovimiento.INGRESO ? "Ingreso registrado." : "Gasto registrado.");
                return "redirect:/movimientos";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarListas(usuario.getId(), model, null);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                         Model model, RedirectAttributes redireccion) {
        try {
            model.addAttribute("formulario", movimientoService.formularioParaEditar(usuario.getId(), id));
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/movimientos";
        }
        cargarListas(usuario.getId(), model, id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                             @Valid @ModelAttribute("formulario") MovimientoForm formulario,
                             BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                movimientoService.actualizar(usuario.getId(), id, formulario);
                redireccion.addFlashAttribute("mensaje", "Movimiento actualizado.");
                return "redirect:/movimientos";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarListas(usuario.getId(), model, id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                           @RequestParam(required = false) String volver, RedirectAttributes redireccion) {
        try {
            movimientoService.eliminar(usuario.getId(), id);
            redireccion.addFlashAttribute("mensaje", "Movimiento eliminado.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return Redirecciones.volverA(volver, "/movimientos");
    }

    private void cargarListas(Long idUsuario, Model model, Long idMovimientoEditado) {
        model.addAttribute("categoriasIngreso", categoriaService.listarActivas(idUsuario, TipoCategoria.INGRESO));
        model.addAttribute("categoriasGasto", categoriaService.listarActivas(idUsuario, TipoCategoria.GASTO));
        model.addAttribute("categoriasCosto", categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO));
        model.addAttribute("fuentes", fuenteService.listarActivas(idUsuario));
        model.addAttribute("cajitas", cajitaService.listarActivas(idUsuario));
        model.addAttribute("idMovimiento", idMovimientoEditado);
    }
}
