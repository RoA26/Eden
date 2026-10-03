package com.eden.modelo;

public enum Frecuencia {
    DIARIA("Diaria"),
    SEMANAL("Semanal"),
    QUINCENAL("Quincenal"),
    MENSUAL("Mensual");

    private final String etiqueta;

    Frecuencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
