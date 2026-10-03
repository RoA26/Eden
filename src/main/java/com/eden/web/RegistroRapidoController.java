package com.eden.web;

import com.eden.dto.ProducidoForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.Categoria;
import com.eden.modelo.FuenteIngreso;
import com.eden.modelo.TipoCategoria;
import com.eden.seguridad.UsuarioAutenticado;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Registro rapido del producido del dia (RN-01), la accion mas frecuente desde el telefono. */
@Controller
public class RegistroRapidoController {

    private static final String VISTA = "movimientos/producido";

    private final MovimientoService movimientoService;
    private final FuenteIngresoService fuenteService;
    private final CategoriaService categoriaService;

    public RegistroRapidoController(MovimientoService movimientoService, FuenteIngresoService fuenteService,
                                    CategoriaService categoriaService) {
        this.movimientoService = movimientoService;
        this.fuenteService = fuenteService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/registrar")
    public String formulario(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        Long idUsuario = usuario.getId();
        cargarListas(idUsuario, model);

        ProducidoForm formulario = new ProducidoForm();
        formulario.setFecha(movimientoService.hoy());
        List<FuenteIngreso> fuentes = fuenteService.listarActivas(idUsuario);
        if (!fuentes.isEmpty()) {
            formulario.setIdFuente(fuentes.get(0).getId());
        }
        formulario.setIdCategoriaIngreso(idPreferido(categoriaService.listarActivas(idUsuario, TipoCategoria.INGRESO), "Producido"));
        formulario.setIdCategoriaCosto(idPreferido(categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO), "Combustible"));
        model.addAttribute("formulario", formulario);
        return VISTA;
    }

    @PostMapping("/registrar")
    public String registrar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                            @Valid @ModelAttribute("formulario") ProducidoForm formulario,
                            BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                movimientoService.registrarProducido(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Producido registrado.");
                return "redirect:/inicio";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        cargarListas(usuario.getId(), model);
        return VISTA;
    }

    private void cargarListas(Long idUsuario, Model model) {
        model.addAttribute("fuentes", fuenteService.listarActivas(idUsuario));
        model.addAttribute("categoriasIngreso", categoriaService.listarActivas(idUsuario, TipoCategoria.INGRESO));
        model.addAttribute("categoriasCosto", categoriaService.listarActivas(idUsuario, TipoCategoria.COSTO_OPERATIVO));
    }

    /** Preselecciona la categoria con el nombre esperado, o la primera disponible. */
    private static Long idPreferido(List<Categoria> categorias, String nombrePreferido) {
        return categorias.stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombrePreferido))
                .findFirst()
                .or(() -> categorias.stream().findFirst())
                .map(Categoria::getId)
                .orElse(null);
    }
}
