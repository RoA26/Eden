package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PresupuestoForm {

    @NotNull(message = "Elige la categoría.")
    private Long idCategoria;

    @NotBlank(message = "Escribe el tope mensual.")
    private String monto;

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }
}
