package com.eden.modelo;

public enum TipoMovimiento {
    INGRESO("Ingreso", true),
    GASTO("Gasto", false),
    APORTE("Aporte a cajita", false),
    RETIRO("Retiro de cajita", true),
    RENDIMIENTO("Rendimiento", true),
    SALDO_INICIAL("Saldo inicial", true);

    private final String etiqueta;
    /** true si, visto desde el movimiento, el dinero "entra" (se muestra en verde). */
    private final boolean entrada;

    TipoMovimiento(String etiqueta, boolean entrada) {
        this.etiqueta = etiqueta;
        this.entrada = entrada;
    }

    public String getEtiqueta() { return etiqueta; }

    public boolean isEntrada() { return entrada; }

    public boolean esIngresoOGasto() {
        return this == INGRESO || this == GASTO;
    }

    public boolean esOperacionDeCajita() {
        return !esIngresoOGasto();
    }
}
