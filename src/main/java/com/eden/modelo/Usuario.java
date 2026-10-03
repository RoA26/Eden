package com.eden.modelo;

import java.time.OffsetDateTime;
import java.util.Locale;

public class Usuario {

    private Long id;
    private String nombreUsuario;
    private String nombreCompleto;
    private String contrasenaHash;
    private boolean activo = true;
    private OffsetDateTime fechaRegistro;

    /**
     * Regla unica de normalizacion del nombre de usuario (sin espacios y en
     * minusculas), usada tanto al registrar como al autenticar.
     */
    public static String normalizarNombreUsuario(String nombreUsuario) {
        return nombreUsuario == null ? null : nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(OffsetDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
