package com.eden.modelo;

import java.math.BigDecimal;

public class Cajita {

    private Long id;
    private Long idUsuario;
    private String nombre;
    private PropositoCajita proposito;
    private BigDecimal porcentaje = BigDecimal.ZERO;
    private boolean esResto;
    private boolean activa = true;
    /** Calculado desde los movimientos (RN-06); nunca se edita directamente. */
    private BigDecimal saldo = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public PropositoCajita getProposito() { return proposito; }
    public void setProposito(PropositoCajita proposito) { this.proposito = proposito; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public boolean isEsResto() { return esResto; }
    public void setEsResto(boolean esResto) { this.esResto = esResto; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}
