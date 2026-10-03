package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Registro rapido del total del dia de una fuente (RN-01), con su combustible opcional. */
public class ProducidoForm {

    @NotNull(message = "Elige la fuente.")
    private Long idFuente;

    @NotNull(message = "Elige la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotBlank(message = "Escribe cuánto hiciste.")
    private String monto;

    @NotNull(message = "Elige la categoría del ingreso.")
    private Long idCategoriaIngreso;

    private String costo;

    private Long idCategoriaCosto;

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public Long getIdCategoriaIngreso() { return idCategoriaIngreso; }
    public void setIdCategoriaIngreso(Long idCategoriaIngreso) { this.idCategoriaIngreso = idCategoriaIngreso; }

    public String getCosto() { return costo; }
    public void setCosto(String costo) { this.costo = costo; }

    public Long getIdCategoriaCosto() { return idCategoriaCosto; }
    public void setIdCategoriaCosto(Long idCategoriaCosto) { this.idCategoriaCosto = idCategoriaCosto; }
}
