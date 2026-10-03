package com.eden.web;

import com.eden.dto.RegistroUsuarioForm;
import com.eden.excepcion.ReglaNegocioException;
import com.eden.servicio.RegistroUsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de inicio de sesion y registro. El POST de /login lo procesa
 * Spring Security (ver SeguridadConfig); aqui solo se muestra la vista.
 */
@Controller
public class AutenticacionController {

    private static final String VISTA_LOGIN = "autenticacion/login";
    private static final String VISTA_REGISTRO = "autenticacion/registro";

    private final RegistroUsuarioService registroUsuarioService;

    public AutenticacionController(RegistroUsuarioService registroUsuarioService) {
        this.registroUsuarioService = registroUsuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin(Authentication autenticacion) {
        return autenticacion != null ? "redirect:/inicio" : VISTA_LOGIN;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Authentication autenticacion, Model model) {
        if (autenticacion != null) {
            return "redirect:/inicio";
        }
        model.addAttribute("formulario", new RegistroUsuarioForm());
        return VISTA_REGISTRO;
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("formulario") RegistroUsuarioForm formulario,
                            BindingResult resultado,
                            RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            formulario.limpiarDatosSensibles();
            return VISTA_REGISTRO;
        }
        try {
            registroUsuarioService.registrar(formulario);
        } catch (ReglaNegocioException e) {
            if (e.tieneCampo()) {
                resultado.rejectValue(e.getCampo(), "regla.negocio", e.getMessage());
            } else {
                resultado.reject("regla.negocio", e.getMessage());
            }
            formulario.limpiarDatosSensibles();
            return VISTA_REGISTRO;
        }
        redireccion.addFlashAttribute("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
