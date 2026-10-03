package com.eden.dto;

import com.eden.modelo.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Aporte, retiro, rendimiento o saldo existente de una cajita. */
public class OperacionCajitaForm {

    @NotNull(message = "Elige la operación.")
    private TipoMovimiento operacion = TipoMovimiento.APORTE;

    @NotBlank(message = "Escribe el monto.")
    private String monto;

    @NotNull(message = "Elige la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Size(max = 200, message = "La descripción admite hasta 200 caracteres.")
    private String descripcion;

    public TipoMovimiento getOperacion() { return operacion; }
    public void setOperacion(TipoMovimiento operacion) { this.operacion = operacion; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
