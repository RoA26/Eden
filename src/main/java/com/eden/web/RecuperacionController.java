package com.eden.web;

import com.eden.dto.RecuperarForm;
import com.eden.dto.RestablecerContrasenaForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.servicio.RecuperacionContrasenaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Flujo publico de "Olvide mi contrasena": pedir PIN y restablecer. */
@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private static final String VISTA_SOLICITAR = "autenticacion/recuperar";
    private static final String VISTA_RESTABLECER = "autenticacion/restablecer";

    private final RecuperacionContrasenaService recuperacionService;

    public RecuperacionController(RecuperacionContrasenaService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @GetMapping
    public String solicitar(Authentication autenticacion, Model model) {
        if (autenticacion != null && autenticacion.isAuthenticated()) {
            return "redirect:/inicio";
        }
        model.addAttribute("formulario", new RecuperarForm());
        return VISTA_SOLICITAR;
    }

    @PostMapping
    public String generarPin(@Valid @ModelAttribute("formulario") RecuperarForm formulario, BindingResult resultado,
                             RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            return VISTA_SOLICITAR;
        }
        recuperacionService.solicitar(formulario.getNombreUsuario());
        // Mismo mensaje exista o no la cuenta.
        redireccion.addFlashAttribute("mensaje",
                "Si la cuenta existe, se generó un PIN de 6 dígitos en la consola del servidor. Vence en 15 minutos.");
        redireccion.addAttribute("usuario", formulario.getNombreUsuario().trim());
        return "redirect:/recuperar/confirmar";
    }

    @GetMapping("/confirmar")
    public String mostrarRestablecer(@RequestParam(required = false) String usuario, Model model) {
        RestablecerContrasenaForm formulario = new RestablecerContrasenaForm();
        formulario.setNombreUsuario(usuario);
        model.addAttribute("formulario", formulario);
        return VISTA_RESTABLECER;
    }

    @PostMapping("/confirmar")
    public String restablecer(@Valid @ModelAttribute("formulario") RestablecerContrasenaForm formulario,
                              BindingResult resultado, RedirectAttributes redireccion) {
        if (!resultado.hasErrors()) {
            try {
                recuperacionService.restablecer(formulario);
                redireccion.addFlashAttribute("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.");
                return "redirect:/login";
            } catch (ReglaNegocioException e) {
                Formularios.rechazar(resultado, e);
            }
        }
        formulario.limpiarDatosSensibles();
        return VISTA_RESTABLECER;
    }
}
