package com.eden.web;

import com.eden.dto.AtajoForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AtajoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Version sin JavaScript de los botones rapidos: los formularios del tablero
 * apuntan aqui y, con JavaScript activo, Alpine los intercepta y usa
 * {@link ApiController} para no recargar la pagina.
 */
@Controller
@RequestMapping("/atajos")
public class AtajoController {

    private final AtajoService atajoService;

    public AtajoController(AtajoService atajoService) {
        this.atajoService = atajoService;
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("atajo") AtajoForm formulario, BindingResult resultado,
                        RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            redireccion.addFlashAttribute("error", resultado.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/inicio";
        }
        try {
            atajoService.crear(usuario.getId(), formulario);
            redireccion.addFlashAttribute("mensaje", "Botón rápido creado.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inicio";
    }

    @PostMapping("/{id}/usar")
    public String usar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                       RedirectAttributes redireccion) {
        try {
            atajoService.usar(usuario.getId(), id);
            redireccion.addFlashAttribute("mensaje", "Registrado.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inicio";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                           RedirectAttributes redireccion) {
        atajoService.eliminar(usuario.getId(), id);
        redireccion.addFlashAttribute("mensaje", "Botón rápido eliminado.");
        return "redirect:/inicio";
    }
}
