package com.eden.web;

import com.eden.dto.FuenteForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Frecuencia;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.FuenteIngresoService;
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
@RequestMapping("/fuentes")
public class FuenteController {

    private static final String VISTA = "fuentes/lista";

    private final FuenteIngresoService fuenteService;

    public FuenteController(FuenteIngresoService fuenteService) {
        this.fuenteService = fuenteService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("formulario", new FuenteForm());
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("formulario") FuenteForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                fuenteService.crear(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Fuente creada. Ya puedes registrar su producido.");
                return "redirect:/fuentes";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping("/{id}")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                             @Valid FuenteForm formulario, BindingResult resultado,
                             RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            redireccion.addFlashAttribute("error", resultado.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/fuentes";
        }
        try {
            fuenteService.actualizar(usuario.getId(), id, formulario);
            redireccion.addFlashAttribute("mensaje", "Fuente actualizada.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fuentes";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                @RequestParam boolean activa, RedirectAttributes redireccion) {
        fuenteService.cambiarEstado(usuario.getId(), id, activa);
        redireccion.addFlashAttribute("mensaje", activa ? "Fuente activada." : "Fuente desactivada.");
        return "redirect:/fuentes";
    }

    private void cargarLista(Long idUsuario, Model model) {
        model.addAttribute("fuentes", fuenteService.listar(idUsuario));
        model.addAttribute("frecuencias", Frecuencia.values());
    }
}
