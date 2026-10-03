package com.eden.web;

import com.eden.dto.ConfirmacionRecurrenteForm;
import com.eden.dto.RecurrenteForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Periodicidad;
import com.eden.modelo.Recurrente;
import com.eden.modelo.TipoCategoria;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CajitaService;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.FuenteIngresoService;
import com.eden.servicio.RecurrenteService;
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
@RequestMapping("/recurrentes")
public class RecurrenteController {

    private static final String VISTA_LISTA = "recurrentes/lista";
    private static final String VISTA_FORMULARIO = "recurrentes/formulario";

    private final RecurrenteService recurrenteService;
    private final CategoriaService categoriaService;
    private final FuenteIngresoService fuenteService;
    private final CajitaService cajitaService;
    private final FormatoVista formato;

    public RecurrenteController(RecurrenteService recurrenteService, CategoriaService categoriaService,
                                FuenteIngresoService fuenteService, CajitaService cajitaService, FormatoVista formato) {
        this.recurrenteService = recurrenteService;
        this.categoriaService = categoriaService;
        this.fuenteService = fuenteService;
        this.cajitaService = cajitaService;
        this.formato = formato;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("recurrentes", recurrenteService.listar(usuario.getId()));
        model.addAttribute("hoy", recurrenteService.hoy());
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        RecurrenteForm formulario = new RecurrenteForm();
        formulario.setProximaFecha(recurrenteService.hoy());
        model.addAttribute("formulario", formulario);
        cargarListas(usuario.getId(), model, null);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("formulario") RecurrenteForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                recurrenteService.crear(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Recurrente programado. Te lo recordaremos en el inicio.");
                return "redirect:/recurrentes";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarListas(usuario.getId(), model, null);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id, Model model) {
        model.addAttribute("formulario", recurrenteService.formularioParaEditar(usuario.getId(), id));
        cargarListas(usuario.getId(), model, id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                             @Valid @ModelAttribute("formulario") RecurrenteForm formulario,
                             BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                recurrenteService.actualizar(usuario.getId(), id, formulario);
                redireccion.addFlashAttribute("mensaje", "Recurrente actualizado.");
                return "redirect:/recurrentes";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarListas(usuario.getId(), model, id);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                            @ModelAttribute ConfirmacionRecurrenteForm confirmacion,
                            @RequestParam(required = false) String volver, RedirectAttributes redireccion) {
        try {
            Recurrente r = recurrenteService.confirmar(usuario.getId(), id, confirmacion);
            redireccion.addFlashAttribute("mensaje", r.getNombre() + " registrado. Próxima fecha: "
                    + formato.fecha(r.getProximaFecha()) + ".");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return Redirecciones.volverA(volver, "/recurrentes");
    }

    @PostMapping("/{id}/omitir")
    public String omitir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                         @RequestParam(required = false) String volver, RedirectAttributes redireccion) {
        Recurrente r = recurrenteService.omitir(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", r.getNombre() + " omitido este periodo.");
        return Redirecciones.volverA(volver, "/recurrentes");
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                @RequestParam boolean activo, RedirectAttributes redireccion) {
        recurrenteService.cambiarEstado(usuario.getId(), id, activo);
        redireccion.addFlashAttribute("mensaje", activo ? "Recurrente reanudado." : "Recurrente pausado.");
        return "redirect:/recurrentes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                           RedirectAttributes redireccion) {
        recurrenteService.eliminar(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", "Recurrente eliminado. Los movimientos ya registrados se conservan.");
        return "redirect:/recurrentes";
    }

    private void cargarListas(Long idUsuario, Model model, Long idRecurrente) {
        model.addAttribute("categoriasIngreso", categoriaService.listarActivas(idUsuario, TipoCategoria.INGRESO));
        model.addAttribute("categoriasGasto", categoriaService.listarActivas(idUsuario, TipoCategoria.GASTO));
        model.addAttribute("categoriasCosto", categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO));
        model.addAttribute("fuentes", fuenteService.listarActivas(idUsuario));
        model.addAttribute("cajitas", cajitaService.listarActivas(idUsuario));
        model.addAttribute("periodicidades", Periodicidad.values());
        model.addAttribute("idRecurrente", idRecurrente);
    }
}
