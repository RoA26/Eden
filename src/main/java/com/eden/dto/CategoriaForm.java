package com.eden.dto;

import com.eden.modelo.TipoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    @NotBlank(message = "Escribe el nombre de la categoría.")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres.")
    private String nombre;

    @NotNull(message = "Elige el tipo de categoría.")
    private TipoCategoria tipo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }
}
