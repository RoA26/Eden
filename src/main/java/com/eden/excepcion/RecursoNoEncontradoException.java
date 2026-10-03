package com.eden.excepcion;

/**
 * El recurso solicitado no existe o no pertenece al usuario autenticado.
 * Ambos casos responden igual (404) para no revelar datos de otros usuarios.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
