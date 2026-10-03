package com.eden.web;

import com.eden.config.PropiedadesApp;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Datos disponibles en todas las vistas Thymeleaf. "soloLectura" y
 * "baseRuta" valen false y "" en la cuenta propia; las vistas compartidas
 * los reemplazan en su controlador.
 */
@ControllerAdvice
public class AtributosGlobales {

    private final PropiedadesApp propiedades;

    public AtributosGlobales(PropiedadesApp propiedades) {
        this.propiedades = propiedades;
    }

    @ModelAttribute("nombreApp")
    public String nombreApp() {
        return propiedades.nombre();
    }

    @ModelAttribute("soloLectura")
    public boolean soloLectura() {
        return false;
    }

    @ModelAttribute("baseRuta")
    public String baseRuta() {
        return "";
    }
}
