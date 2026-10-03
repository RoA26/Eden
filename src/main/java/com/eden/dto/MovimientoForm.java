package com.eden.dto;

import com.eden.modelo.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Ingreso o gasto registrado desde el formulario general. */
public class MovimientoForm {

    @NotNull(message = "Elige si es ingreso o gasto.")
    private TipoMovimiento tipo;

    @NotNull(message = "Elige la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotBlank(message = "Escribe el monto.")
    private String monto;

    @NotNull(message = "Elige una categoría.")
    private Long idCategoria;

    private Long idFuente;

    /** Solo para gastos nuevos: cajita de la que sale el dinero (RN-06). */
    private Long idCajitaOrigen;

    @Size(max = 200, message = "La descripción admite hasta 200 caracteres.")
    private String descripcion;

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public Long getIdCajitaOrigen() { return idCajitaOrigen; }
    public void setIdCajitaOrigen(Long idCajitaOrigen) { this.idCajitaOrigen = idCajitaOrigen; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
