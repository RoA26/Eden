package com.eden.web;

import com.eden.dto.AtajoForm;
import com.eden.dto.CategoriaForm;
import com.eden.dto.MovimientoForm;
import com.eden.dto.ResumenSaldo;
import com.eden.modelo.Atajo;
import com.eden.modelo.Categoria;
import com.eden.modelo.Dinero;
import com.eden.modelo.TipoMovimiento;
import com.eden.seguridad.UsuarioAutenticado;
import com.eden.servicio.AtajoService;
import com.eden.servicio.CategoriaService;
import com.eden.servicio.MovimientoService;
import com.eden.servicio.TableroService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Acciones del tablero que se ejecutan sin recargar la pagina (fetch desde
 * Alpine.js). Usan los mismos servicios y reglas que los formularios
 * clasicos; las respuestas son JSON y los errores los arma {@link ApiErrores}.
 * El token CSRF viaja en el propio formulario (campo _csrf) o en la cabecera
 * X-CSRF-TOKEN, igual que en cualquier POST de la aplicacion.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final AtajoService atajoService;
    private final MovimientoService movimientoService;
    private final CategoriaService categoriaService;
    private final TableroService tableroService;

    public ApiController(AtajoService atajoService, MovimientoService movimientoService,
                         CategoriaService categoriaService, TableroService tableroService) {
        this.atajoService = atajoService;
        this.movimientoService = movimientoService;
        this.categoriaService = categoriaService;
        this.tableroService = tableroService;
    }

    // ------------------------------------------------------------------ botones rapidos

    @PostMapping("/atajos/{id}/usar")
    public Map<String, Object> usarAtajo(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        Atajo atajo = atajoService.obtener(usuario.getId(), id);
        Long idMovimiento = atajoService.usar(usuario.getId(), id);
        String signo = atajo.esIngreso() ? "+ " : "− ";
        return respuestaConSaldo(usuario.getId(),
                atajo.getNombre() + " · " + signo + Dinero.formatear(atajo.getMonto()), idMovimiento, atajo.getTipo());
    }

    @PostMapping("/atajos")
    public ResponseEntity<Map<String, Object>> crearAtajo(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                          @Valid @ModelAttribute AtajoForm formulario,
                                                          BindingResult resultado) {
        if (resultado.hasErrors()) {
            return ApiErrores.validacion(resultado);
        }
        Atajo atajo = atajoService.crear(usuario.getId(), formulario);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Botón «" + atajo.getNombre() + "» creado.");
        respuesta.put("id", atajo.getId());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/atajos/{id}/eliminar")
    public Map<String, Object> eliminarAtajo(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        atajoService.eliminar(usuario.getId(), id);
        return Map.of("mensaje", "Botón eliminado.");
    }

    // ------------------------------------------------------------------ movimientos

    @PostMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> registrarMovimiento(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                                   @Valid @ModelAttribute MovimientoForm formulario,
                                                                   BindingResult resultado) {
        if (resultado.hasErrors()) {
            return ApiErrores.validacion(resultado);
        }
        Long idMovimiento = movimientoService.registrar(usuario.getId(), formulario);
        String mensaje = formulario.getTipo() == TipoMovimiento.INGRESO ? "Ingreso registrado." : "Gasto registrado.";
        return ResponseEntity.ok(respuestaConSaldo(usuario.getId(), mensaje, idMovimiento, formulario.getTipo()));
    }

    /** Usado por "Deshacer" tras un registro rapido. */
    @PostMapping("/movimientos/{id}/eliminar")
    public Map<String, Object> eliminarMovimiento(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        movimientoService.eliminar(usuario.getId(), id);
        return respuestaConSaldo(usuario.getId(), "Registro deshecho.", null, null);
    }

    // ------------------------------------------------------------------ categorias

    @PostMapping("/categorias")
    public ResponseEntity<Map<String, Object>> crearCategoria(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                              @Valid @ModelAttribute CategoriaForm formulario,
                                                              BindingResult resultado) {
        if (resultado.hasErrors()) {
            return ApiErrores.validacion(resultado);
        }
        Categoria categoria = categoriaService.crear(usuario.getId(), formulario);
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("id", categoria.getId());
        datos.put("nombre", categoria.getNombre());
        datos.put("tipo", categoria.getTipo().name());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Categoría «" + categoria.getNombre() + "» creada.");
        respuesta.put("categoria", datos);
        return ResponseEntity.ok(respuesta);
    }

    // ------------------------------------------------------------------ apoyo

    private Map<String, Object> respuestaConSaldo(Long idUsuario, String mensaje, Long idMovimiento, TipoMovimiento tipo) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", mensaje);
        if (idMovimiento != null) {
            respuesta.put("idMovimiento", idMovimiento);
        }
        if (tipo != null) {
            respuesta.put("tipo", tipo.name());
        }
        respuesta.put("saldo", saldo(tableroService.saldo(idUsuario)));
        return respuesta;
    }

    /** Cifras crudas para animar el contador y su texto ya formateado igual que en las plantillas. */
    private static Map<String, Object> saldo(ResumenSaldo s) {
        Map<String, Object> cifras = new LinkedHashMap<>();
        agregar(cifras, "total", s.total());
        agregar(cifras, "disponible", s.disponible());
        agregar(cifras, "enCajitas", s.enCajitas());
        agregar(cifras, "ingresosMes", s.mes().ingresos());
        agregar(cifras, "gastosMes", s.mes().gastosTotales());
        agregar(cifras, "balanceMes", s.mes().balance());
        return cifras;
    }

    private static void agregar(Map<String, Object> cifras, String nombre, BigDecimal valor) {
        cifras.put(nombre, Map.of("valor", valor, "texto", Dinero.formatear(valor)));
    }
}
