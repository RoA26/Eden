package com.eden.web;

import com.eden.dto.MetaForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CajitaService;
import com.eden.servicio.MetaService;
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
@RequestMapping("/metas")
public class MetaController {

    private static final String VISTA_LISTA = "metas/lista";
    private static final String VISTA_FORMULARIO = "metas/formulario";

    private final MetaService metaService;
    private final CajitaService cajitaService;

    public MetaController(MetaService metaService, CajitaService cajitaService) {
        this.metaService = metaService;
        this.cajitaService = cajitaService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("metas", metaService.listar(usuario.getId()));
        return VISTA_LISTA;
    }

    @GetMapping("/nueva")
    public String nueva(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("formulario", new MetaForm());
        return cargarFormulario(usuario.getId(), model, null);
    }

    @PostMapping("/nueva")
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("formulario") MetaForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                metaService.crear(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Meta creada.");
                return "redirect:/metas";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        return cargarFormulario(usuario.getId(), model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id, Model model) {
        model.addAttribute("formulario", metaService.formularioParaEditar(usuario.getId(), id));
        return cargarFormulario(usuario.getId(), model, id);
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                             @Valid @ModelAttribute("formulario") MetaForm formulario,
                             BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                metaService.actualizar(usuario.getId(), id, formulario);
                redireccion.addFlashAttribute("mensaje", "Meta actualizada.");
                return "redirect:/metas";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        return cargarFormulario(usuario.getId(), model, id);
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                @RequestParam boolean activa, RedirectAttributes redireccion) {
        metaService.cambiarEstado(usuario.getId(), id, activa);
        redireccion.addFlashAttribute("mensaje", activa ? "Meta reactivada." : "Meta archivada.");
        return "redirect:/metas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                           RedirectAttributes redireccion) {
        metaService.eliminar(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", "Meta eliminada. El dinero sigue en su cajita.");
        return "redirect:/metas";
    }

    private String cargarFormulario(Long idUsuario, Model model, Long idMeta) {
        model.addAttribute("cajitas", cajitaService.listarActivas(idUsuario));
        model.addAttribute("idMeta", idMeta);
        return VISTA_FORMULARIO;
    }
}
