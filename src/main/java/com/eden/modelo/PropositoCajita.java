package com.eden.modelo;

public enum PropositoCajita {
    CAPITAL("Capital"),
    COMPRAS("Compras esenciales"),
    EMERGENCIA("Emergencia"),
    LIBRE("Libre");

    private final String etiqueta;

    PropositoCajita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
