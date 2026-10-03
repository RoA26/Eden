package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class MetaForm {

    @NotBlank(message = "Escribe el nombre de la meta.")
    @Size(max = 60, message = "El nombre admite hasta 60 caracteres.")
    private String nombre;

    @NotBlank(message = "Escribe cuánto quieres reunir.")
    private String montoObjetivo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaObjetivo;

    @NotNull(message = "Elige la cajita donde se ahorra para esta meta.")
    private Long idCajita;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(String montoObjetivo) { this.montoObjetivo = montoObjetivo; }

    public LocalDate getFechaObjetivo() { return fechaObjetivo; }
    public void setFechaObjetivo(LocalDate fechaObjetivo) { this.fechaObjetivo = fechaObjetivo; }

    public Long getIdCajita() { return idCajita; }
    public void setIdCajita(Long idCajita) { this.idCajita = idCajita; }
}
