package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompartirForm {

    @NotBlank(message = "Escribe el nombre de usuario de la persona.")
    @Size(max = 30, message = "El nombre de usuario admite hasta 30 caracteres.")
    private String nombreUsuario;

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
