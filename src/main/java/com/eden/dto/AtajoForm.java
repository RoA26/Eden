package com.eden.dto;

import com.eden.modelo.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nuevo boton rapido: nombre visible, tipo, monto fijo, categoria y fuente opcional. */
public class AtajoForm {

    @NotBlank(message = "Escribe el nombre del botón.")
    @Size(max = 30, message = "El nombre admite hasta 30 caracteres.")
    private String nombre;

    @NotNull(message = "Elige si es ingreso o gasto.")
    private TipoMovimiento tipo;

    @NotBlank(message = "Escribe el monto.")
    private String monto;

    @NotNull(message = "Elige una categoría.")
    private Long idCategoria;

    private Long idFuente;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }
}
