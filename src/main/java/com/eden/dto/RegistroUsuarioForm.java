package com.eden.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del formulario de registro. Las anotaciones cubren el formato;
 * las reglas de negocio (codigo de invitacion, usuario repetido,
 * confirmacion de contrasena) se validan en RegistroUsuarioService.
 */
public class RegistroUsuarioForm {

    @NotBlank(message = "Escribe tu nombre.")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
    private String nombreCompleto;

    @NotBlank(message = "Escribe un nombre de usuario.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,30}$",
             message = "Usa de 3 a 30 letras, números, puntos, guiones o guiones bajos, sin espacios.")
    private String nombreUsuario;

    @NotBlank(message = "Escribe una contraseña.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String contrasena;

    @NotBlank(message = "Confirma la contraseña.")
    private String confirmacionContrasena;

    @NotBlank(message = "Escribe el código de invitación.")
    private String codigoInvitacion;

    /** Evita devolver contraseñas y codigo a la vista cuando el formulario se re-renderiza. */
    public void limpiarDatosSensibles() {
        this.contrasena = null;
        this.confirmacionContrasena = null;
        this.codigoInvitacion = null;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getConfirmacionContrasena() { return confirmacionContrasena; }
    public void setConfirmacionContrasena(String confirmacionContrasena) { this.confirmacionContrasena = confirmacionContrasena; }

    public String getCodigoInvitacion() { return codigoInvitacion; }
    public void setCodigoInvitacion(String codigoInvitacion) { this.codigoInvitacion = codigoInvitacion; }
}
