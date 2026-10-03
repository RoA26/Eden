package com.eden.excepcion;

/**
 * Violacion de una regla de negocio. Si se conoce el campo del formulario
 * afectado, el controlador puede asociar el mensaje a ese campo.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String campo;

    public ReglaNegocioException(String mensaje) {
        this(null, mensaje);
    }

    public ReglaNegocioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }

    public boolean tieneCampo() {
        return campo != null;
    }
}
