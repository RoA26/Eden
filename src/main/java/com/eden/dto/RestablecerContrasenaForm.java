package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PIN recibido + contrasena nueva. Las reglas de la contrasena son las mismas del registro. */
public class RestablecerContrasenaForm {

    @NotBlank(message = "Escribe tu nombre de usuario.")
    @Size(max = 30, message = "El nombre de usuario admite hasta 30 caracteres.")
    private String nombreUsuario;

    @NotBlank(message = "Escribe el PIN.")
    @Pattern(regexp = "^\\s*\\d{6}\\s*$", message = "El PIN tiene 6 dígitos.")
    private String pin;

    @NotBlank(message = "Escribe la nueva contraseña.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String contrasena;

    @NotBlank(message = "Confirma la nueva contraseña.")
    private String confirmacionContrasena;

    /** Evita devolver PIN y contrasenas a la vista cuando el formulario se re-renderiza. */
    public void limpiarDatosSensibles() {
        this.pin = null;
        this.contrasena = null;
        this.confirmacionContrasena = null;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getConfirmacionContrasena() { return confirmacionContrasena; }
    public void setConfirmacionContrasena(String confirmacionContrasena) { this.confirmacionContrasena = confirmacionContrasena; }
}
