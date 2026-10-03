package com.eden.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Recurrente {

    private Long id;
    private Long idUsuario;
    private String nombre;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private Long idCategoria;
    private Long idFuente;
    private Long idCajitaOrigen;
    private Periodicidad periodicidad;
    private int diaAncla;
    private LocalDate proximaFecha;
    private int diasAviso = 3;
    private boolean activo = true;

    // Datos descriptivos (JOIN)
    private String nombreCategoria;
    private String nombreFuente;
    private String nombreCajitaOrigen;

    /** true si ya llego (o esta dentro de los dias de aviso) su proxima fecha. */
    public boolean estaPendiente(LocalDate hoy) {
        return activo && !proximaFecha.minusDays(diasAviso).isAfter(hoy);
    }

    public boolean estaVencido(LocalDate hoy) {
        return proximaFecha.isBefore(hoy);
    }

    public void avanzar() {
        this.proximaFecha = periodicidad.siguiente(proximaFecha, diaAncla);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public Long getIdCajitaOrigen() { return idCajitaOrigen; }
    public void setIdCajitaOrigen(Long idCajitaOrigen) { this.idCajitaOrigen = idCajitaOrigen; }

    public Periodicidad getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(Periodicidad periodicidad) { this.periodicidad = periodicidad; }

    public int getDiaAncla() { return diaAncla; }
    public void setDiaAncla(int diaAncla) { this.diaAncla = diaAncla; }

    public LocalDate getProximaFecha() { return proximaFecha; }
    public void setProximaFecha(LocalDate proximaFecha) { this.proximaFecha = proximaFecha; }

    public int getDiasAviso() { return diasAviso; }
    public void setDiasAviso(int diasAviso) { this.diasAviso = diasAviso; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public String getNombreFuente() { return nombreFuente; }
    public void setNombreFuente(String nombreFuente) { this.nombreFuente = nombreFuente; }

    public String getNombreCajitaOrigen() { return nombreCajitaOrigen; }
    public void setNombreCajitaOrigen(String nombreCajitaOrigen) { this.nombreCajitaOrigen = nombreCajitaOrigen; }
}
