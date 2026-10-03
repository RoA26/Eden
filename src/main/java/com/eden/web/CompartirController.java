package com.eden.web;

import com.eden.dto.CompartirForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AccesoCompartidoService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** El titular administra quien puede ver su cuenta en modo solo lectura. */
@Controller
@RequestMapping("/compartir")
public class CompartirController {

    private static final String VISTA = "compartir/lista";

    private final AccesoCompartidoService accesoService;

    public CompartirController(AccesoCompartidoService accesoService) {
        this.accesoService = accesoService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("formulario", new CompartirForm());
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping
    public String conceder(@AuthenticationPrincipal UsuarioAutenticado usuario,
                           @Valid @ModelAttribute("formulario") CompartirForm formulario,
                           BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                accesoService.conceder(usuario.getId(), formulario.getNombreUsuario());
                redireccion.addFlashAttribute("mensaje", "Listo. Esa persona ya puede ver tu cuenta (solo lectura).");
                return "redirect:/compartir";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping("/{id}/revocar")
    public String revocar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                          RedirectAttributes redireccion) {
        accesoService.revocar(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", "Acceso revocado.");
        return "redirect:/compartir";
    }

    private void cargarLista(Long idUsuario, Model model) {
        model.addAttribute("concedidos", accesoService.concedidos(idUsuario));
        model.addAttribute("recibidos", accesoService.recibidos(idUsuario));
    }
}
