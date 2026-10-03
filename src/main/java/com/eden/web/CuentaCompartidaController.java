package com.eden.web;

import com.eden.dto.FiltroMovimientos;
import com.eden.modelo.AccesoCompartido;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AccesoCompartidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Consulta de SOLO LECTURA de la cuenta de otra persona. Solo expone GET y
 * cada peticion verifica primero el permiso. Las vistas reutilizadas ocultan
 * sus acciones con la bandera "soloLectura"; aun si alguien forzara un POST,
 * todas las escrituras se filtran por el id del usuario autenticado (RN-07).
 */
@Controller
@RequestMapping("/compartido/{idTitular}")
public class CuentaCompartidaController {

    private final AccesoCompartidoService accesoService;
    private final VistasCuenta vistas;

    public CuentaCompartidaController(AccesoCompartidoService accesoService, VistasCuenta vistas) {
        this.accesoService = accesoService;
        this.vistas = vistas;
    }

    @GetMapping({"", "/inicio"})
    public String tablero(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idTitular, Model model) {
        prepararSoloLectura(usuario, idTitular, model);
        return vistas.tablero(idTitular, model);
    }

    @GetMapping("/movimientos")
    public String movimientos(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idTitular,
                              @ModelAttribute("filtro") FiltroMovimientos filtro, Model model) {
        prepararSoloLectura(usuario, idTitular, model);
        return vistas.movimientos(idTitular, filtro, model);
    }

    @GetMapping("/calendario")
    public String calendario(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long idTitular,
                             @RequestParam(required = false) String mes, Model model) {
        prepararSoloLectura(usuario, idTitular, model);
        return vistas.calendario(idTitular, mes, model);
    }

    private void prepararSoloLectura(UsuarioAutenticado usuario, Long idTitular, Model model) {
        AccesoCompartido acceso = accesoService.verificar(usuario.getId(), idTitular);
        model.addAttribute("soloLectura", true);
        model.addAttribute("baseRuta", "/compartido/" + idTitular);
        model.addAttribute("nombreTitular", acceso.nombreCompletoTitular());
        model.addAttribute("nombreCompleto", acceso.nombreCompletoTitular());
    }
}
