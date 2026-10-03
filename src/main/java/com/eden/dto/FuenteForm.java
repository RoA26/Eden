package com.eden.dto;

import com.eden.modelo.Frecuencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FuenteForm {

    @NotBlank(message = "Escribe el nombre de la fuente.")
    @Size(max = 60, message = "El nombre admite hasta 60 caracteres.")
    private String nombre;

    @NotNull(message = "Elige cada cuánto recibes este ingreso.")
    private Frecuencia frecuencia = Frecuencia.DIARIA;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia frecuencia) { this.frecuencia = frecuencia; }
}
