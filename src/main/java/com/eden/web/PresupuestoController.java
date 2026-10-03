package com.eden.web;

import com.eden.dto.EstadoPresupuesto;
import com.eden.dto.PresupuestoForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.TipoCategoria;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.PresupuestoService;
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

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequestMapping("/presupuestos")
public class PresupuestoController {

    private static final String VISTA = "presupuestos/lista";

    private final PresupuestoService presupuestoService;
    private final CategoriaService categoriaService;

    public PresupuestoController(PresupuestoService presupuestoService, CategoriaService categoriaService) {
        this.presupuestoService = presupuestoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                         @RequestParam(required = false) String mes, Model model) {
        model.addAttribute("formulario", new PresupuestoForm());
        cargarLista(usuario.getId(), mes, model);
        return VISTA;
    }

    @PostMapping
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                          @Valid @ModelAttribute("formulario") PresupuestoForm formulario,
                          BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                presupuestoService.guardar(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Presupuesto guardado.");
                return "redirect:/presupuestos";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarLista(usuario.getId(), null, model);
        return VISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                           RedirectAttributes redireccion) {
        presupuestoService.eliminar(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", "Presupuesto eliminado.");
        return "redirect:/presupuestos";
    }

    private void cargarLista(Long idUsuario, String mesTexto, Model model) {
        YearMonth actual = presupuestoService.mesActual();
        YearMonth mes;
        try {
            mes = mesTexto == null || mesTexto.isBlank() ? actual : YearMonth.parse(mesTexto);
        } catch (DateTimeParseException e) {
            mes = actual;
        }
        List<EstadoPresupuesto> estados = presupuestoService.estado(idUsuario, mes);
        model.addAttribute("mes", mes);
        model.addAttribute("esMesActual", mes.equals(actual));
        model.addAttribute("presupuestos", estados);
        model.addAttribute("totalPresupuestado", PresupuestoService.total(estados, false));
        model.addAttribute("totalGastado", PresupuestoService.total(estados, true));
        model.addAttribute("categoriasGasto", categoriaService.listarActivas(idUsuario, TipoCategoria.GASTO));
        model.addAttribute("categoriasCosto", categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO));
    }
}
