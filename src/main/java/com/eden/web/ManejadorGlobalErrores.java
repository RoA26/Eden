package com.eden.web;

import com.eden.config.PropiedadesApp;
import com.eden.excepcion.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce excepciones no controladas a una pagina de error clara. Los
 * errores inesperados se registran completos en el log, pero al usuario
 * nunca se le muestran detalles tecnicos.
 */
@ControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);
    private static final String VISTA_ERROR = "error";

    private final PropiedadesApp propiedades;

    public ManejadorGlobalErrores(PropiedadesApp propiedades) {
        this.propiedades = propiedades;
    }

    @ExceptionHandler({RecursoNoEncontradoException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(Model model) {
        return vistaError(model, 404, "No encontramos lo que buscas.");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String solicitudInvalida(Exception e, Model model) {
        log.warn("Solicitud invalida: {}", e.getMessage());
        return vistaError(model, 400, "La solicitud tiene datos inválidos.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorInesperado(Exception e, Model model) {
        log.error("Error inesperado", e);
        return vistaError(model, 500, "Ocurrió un error inesperado. Intenta de nuevo en unos minutos.");
    }

    private String vistaError(Model model, int codigo, String mensaje) {
        model.addAttribute("nombreApp", propiedades.nombre());
        model.addAttribute("codigo", codigo);
        model.addAttribute("mensaje", mensaje);
        return VISTA_ERROR;
    }
}
