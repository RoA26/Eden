package com.eden.dto;

import com.eden.modelo.PropositoCajita;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CajitaForm {

    @NotBlank(message = "Escribe el nombre de la cajita.")
    @Size(max = 40, message = "El nombre admite hasta 40 caracteres.")
    private String nombre;

    @NotNull(message = "Elige el propósito de la cajita.")
    private PropositoCajita proposito = PropositoCajita.CAPITAL;

    @NotNull(message = "Escribe el porcentaje (puede ser 0).")
    @DecimalMin(value = "0", message = "El porcentaje no puede ser negativo.")
    @DecimalMax(value = "100", message = "El porcentaje no puede pasar de 100.")
    private BigDecimal porcentaje = BigDecimal.ZERO;

    private boolean esResto;

    /** Solo al crear: dinero que la cajita ya tiene en Nu antes de usar Eden. */
    private String saldoInicial;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public PropositoCajita getProposito() { return proposito; }
    public void setProposito(PropositoCajita proposito) { this.proposito = proposito; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public boolean isEsResto() { return esResto; }
    public void setEsResto(boolean esResto) { this.esResto = esResto; }

    public String getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(String saldoInicial) { this.saldoInicial = saldoInicial; }
}
