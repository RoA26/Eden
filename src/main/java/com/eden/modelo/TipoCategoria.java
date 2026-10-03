package com.eden.modelo;

/**
 * INGRESO y GASTO son movimientos personales; COSTO_OPERATIVO son los
 * gastos propios de una fuente de ingreso (gasolina, repuestos, SOAT...).
 */
public enum TipoCategoria {
    INGRESO("Ingreso"),
    GASTO("Gasto personal"),
    COSTO_OPERATIVO("Costo operativo");

    private final String etiqueta;

    TipoCategoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
