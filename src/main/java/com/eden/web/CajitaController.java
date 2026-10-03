package com.eden.web;

import com.eden.dto.CajitaForm;
import com.eden.dto.OperacionCajitaForm;
import com.eden.dto.PropuestaReparto;
import com.eden.dto.RepartoForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.modelo.CalculadoraReparto;
import com.eden.modelo.Dinero;
import com.eden.modelo.PropositoCajita;
import com.eden.modelo.TipoMovimiento;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.CajitaService;
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

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cajitas")
public class CajitaController {

    private static final String VISTA_LISTA = "cajitas/lista";
    private static final String VISTA_DETALLE = "cajitas/detalle";
    private static final String VISTA_REPARTO = "cajitas/reparto";
    private static final List<TipoMovimiento> OPERACIONES = List.of(
            TipoMovimiento.APORTE, TipoMovimiento.RETIRO, TipoMovimiento.RENDIMIENTO, TipoMovimiento.SALDO_INICIAL);

    private final CajitaService cajitaService;

    public CajitaController(CajitaService cajitaService) {
        this.cajitaService = cajitaService;
    }

    // ------------------------------------------------------------------ lista y creacion

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("formulario", new CajitaForm());
        cargarLista(usuario.getId(), model);
        return VISTA_LISTA;
    }

    @PostMapping
    public String crear(@AuthenticationPrincipal UsuarioAutenticado usuario,
                        @Valid @ModelAttribute("formulario") CajitaForm formulario,
                        BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                cajitaService.crear(usuario.getId(), formulario);
                redireccion.addFlashAttribute("mensaje", "Cajita creada.");
                return "redirect:/cajitas";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        model.addAttribute("abrirFormulario", true);
        cargarLista(usuario.getId(), model);
        return VISTA_LISTA;
    }

    // ------------------------------------------------------------------ detalle

    @GetMapping("/{id}")
    public String detalle(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id, Model model) {
        OperacionCajitaForm operacion = new OperacionCajitaForm();
        operacion.setFecha(cajitaService.hoy());
        model.addAttribute("operacion", operacion);
        model.addAttribute("formulario", cajitaService.formularioParaEditar(usuario.getId(), id));
        return cargarDetalle(usuario.getId(), id, model);
    }

    @PostMapping("/{id}/operacion")
    public String registrarOperacion(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                     @Valid @ModelAttribute("operacion") OperacionCajitaForm operacion,
                                     BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                cajitaService.registrarOperacion(usuario.getId(), id, operacion);
                redireccion.addFlashAttribute("mensaje", operacion.getOperacion().getEtiqueta() + " registrado.");
                return "redirect:/cajitas/" + id;
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        model.addAttribute("formulario", cajitaService.formularioParaEditar(usuario.getId(), id));
        return cargarDetalle(usuario.getId(), id, model);
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                             @Valid @ModelAttribute("formulario") CajitaForm formulario,
                             BindingResult resultado, Model model, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                cajitaService.actualizar(usuario.getId(), id, formulario);
                redireccion.addFlashAttribute("mensaje", "Cajita actualizada.");
                return "redirect:/cajitas/" + id;
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        OperacionCajitaForm operacion = new OperacionCajitaForm();
        operacion.setFecha(cajitaService.hoy());
        model.addAttribute("operacion", operacion);
        model.addAttribute("abrirEdicion", true);
        return cargarDetalle(usuario.getId(), id, model);
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id,
                                @RequestParam boolean activa, RedirectAttributes redireccion) {
        try {
            cajitaService.cambiarEstado(usuario.getId(), id, activa);
            redireccion.addFlashAttribute("mensaje", activa ? "Cajita activada." : "Cajita desactivada.");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cajitas/" + id;
    }

    // ------------------------------------------------------------------ reparto

    @GetMapping("/repartir")
    public String proponerReparto(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                  @RequestParam(required = false) String monto, Model model) {
        PropuestaReparto propuesta;
        try {
            propuesta = cajitaService.proponerReparto(usuario.getId(), monto);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            propuesta = cajitaService.proponerReparto(usuario.getId(), null);
        }
        RepartoForm formulario = new RepartoForm();
        for (CalculadoraReparto.Linea linea : propuesta.propuesta().lineas()) {
            formulario.getLineas().add(new RepartoForm.Linea(linea.cajita().getId(), sinDecimales(linea.monto())));
        }
        model.addAttribute("reparto", propuesta);
        model.addAttribute("formulario", formulario);
        return VISTA_REPARTO;
    }

    @PostMapping("/repartir")
    public String confirmarReparto(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                   @ModelAttribute("formulario") RepartoForm formulario,
                                   RedirectAttributes redireccion) {
        try {
            BigDecimal total = cajitaService.confirmarReparto(usuario.getId(), formulario);
            redireccion.addFlashAttribute("mensaje", "Repartiste " + Dinero.formatear(total) + " en tus cajitas.");
            return "redirect:/inicio";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
            return "redirect:/cajitas/repartir";
        }
    }

    // ------------------------------------------------------------------ utilidades

    private void cargarLista(Long idUsuario, Model model) {
        model.addAttribute("cajitas", cajitaService.listar(idUsuario));
        model.addAttribute("disponible", cajitaService.disponible(idUsuario));
        model.addAttribute("propositos", PropositoCajita.values());
    }

    private String cargarDetalle(Long idUsuario, Long idCajita, Model model) {
        model.addAttribute("cajita", cajitaService.obtener(idUsuario, idCajita));
        model.addAttribute("movimientos", cajitaService.movimientos(idUsuario, idCajita));
        model.addAttribute("disponible", cajitaService.disponible(idUsuario));
        model.addAttribute("operaciones", OPERACIONES);
        model.addAttribute("propositos", PropositoCajita.values());
        return VISTA_DETALLE;
    }

    private static String sinDecimales(BigDecimal monto) {
        return monto.setScale(0, java.math.RoundingMode.DOWN).toPlainString();
    }
}
