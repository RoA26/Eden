package com.eden.web;

import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AccesoCompartidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    private final VistasCuenta vistas;
    private final AccesoCompartidoService accesoService;

    public InicioController(VistasCuenta vistas, AccesoCompartidoService accesoService) {
        this.vistas = vistas;
        this.accesoService = accesoService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    @GetMapping("/inicio")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("nombreCompleto", usuario.getNombreCompleto());
        return vistas.tablero(usuario.getId(), model);
    }

    @GetMapping("/mas")
    public String mas(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("cuentasCompartidas", accesoService.recibidos(usuario.getId()));
        return "mas";
    }
}
