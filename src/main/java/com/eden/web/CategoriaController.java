package com.eden.web;

import com.eden.dto.CategoriaForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.TipoCategoria;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CategoriaService;
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
@RequestMapping("/categorias")
public class CategoriaController {

    private static final String VISTA = "categorias/lista";

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        CategoriaForm formulario = new CategoriaForm();
        formulario.setTipo(TipoCategoria.GASTO);
        model.addAttribute("formulario", formulario);
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("formulario") CategoriaForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                categoriaService.crear(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Categoría creada.");
                return "redirect:/categorias";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarLista(usuario.getId(), model);
        return VISTA;
    }

    @PostMapping("/{id}/renombrar")
    public String renombrar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                            @RequestParam String nombre, RedirectAttributes redireccion) {
        try {
            categoriaService.renombrar(usuario.getId(), id, nombre);
            redireccion.addFlashAttribute("mensaje", "Categoría actualizada.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                @RequestParam boolean activa, RedirectAttributes redireccion) {
        categoriaService.cambiarEstado(usuario.getId(), id, activa);
        redireccion.addFlashAttribute("mensaje", activa ? "Categoría activada." : "Categoría desactivada.");
        return "redirect:/categorias";
    }

    private void cargarLista(Long idUsuario, Model model) {
        model.addAttribute("categorias", categoriaService.listar(idUsuario));
        model.addAttribute("tipos", TipoCategoria.values());
    }
}
