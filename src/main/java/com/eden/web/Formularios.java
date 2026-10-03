package com.eden.web;

import com.eden.excepcion.ReglaNegocioException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.BindingResult;

/** Traslada una violacion de regla de negocio al formulario que se esta re-renderizando. */
final class Formularios {

    private Formularios() {
    }

    static void rechazar(BindingResult resultado, ReglaNegocioException e) {
        Object objetivo = resultado.getTarget();
        boolean campoValido = e.tieneCampo() && objetivo != null
                && new BeanWrapperImpl(objetivo).isReadableProperty(e.getCampo());
        if (campoValido) {
            resultado.rejectValue(e.getCampo(), "regla.negocio", e.getMessage());
        } else {
            resultado.reject("regla.negocio", e.getMessage());
        }
    }
}
