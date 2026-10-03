package com.eden.web;

import com.eden.seguridad.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarioController {

    private final VistasCuenta vistas;

    public CalendarioController(VistasCuenta vistas) {
        this.vistas = vistas;
    }

    @GetMapping("/calendario")
    public String calendario(@AuthenticationPrincipal UsuarioAutenticado usuario,
                             @RequestParam(required = false) String mes, Model model) {
        return vistas.calendario(usuario.getId(), mes, model);
    }
}
