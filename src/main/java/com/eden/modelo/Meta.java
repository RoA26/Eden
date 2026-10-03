package com.eden.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Meta {

    private Long id;
    private Long idUsuario;
    private String nombre;
    private BigDecimal montoObjetivo;
    private LocalDate fechaObjetivo;
    private Long idCajita;
    private boolean activa = true;

    // Datos de la cajita vinculada (JOIN)
    private String nombreCajita;
    private BigDecimal saldoCajita = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(BigDecimal montoObjetivo) { this.montoObjetivo = montoObjetivo; }

    public LocalDate getFechaObjetivo() { return fechaObjetivo; }
    public void setFechaObjetivo(LocalDate fechaObjetivo) { this.fechaObjetivo = fechaObjetivo; }

    public Long getIdCajita() { return idCajita; }
    public void setIdCajita(Long idCajita) { this.idCajita = idCajita; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getNombreCajita() { return nombreCajita; }
    public void setNombreCajita(String nombreCajita) { this.nombreCajita = nombreCajita; }

    public BigDecimal getSaldoCajita() { return saldoCajita; }
    public void setSaldoCajita(BigDecimal saldoCajita) { this.saldoCajita = saldoCajita; }
}
