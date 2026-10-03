package com.eden.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Al confirmar un recurrente el usuario puede ajustar el monto (ej. la factura de luz) y la fecha. */
public class ConfirmacionRecurrenteForm {

    private String monto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
