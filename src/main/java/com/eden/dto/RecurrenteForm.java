package com.eden.dto;

import com.eden.modelo.Periodicidad;
import com.eden.modelo.TipoMovimiento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class RecurrenteForm {

    @NotBlank(message = "Escribe un nombre, por ejemplo \"Arriendo\".")
    @Size(max = 60, message = "El nombre admite hasta 60 caracteres.")
    private String nombre;

    @NotNull(message = "Elige si es un gasto o un ingreso.")
    private TipoMovimiento tipo = TipoMovimiento.GASTO;

    @NotBlank(message = "Escribe el monto habitual.")
    private String monto;

    @NotNull(message = "Elige una categoría.")
    private Long idCategoria;

    private Long idFuente;

    private Long idCajitaOrigen;

    @NotNull(message = "Elige cada cuánto se repite.")
    private Periodicidad periodicidad = Periodicidad.MENSUAL;

    @NotNull(message = "Elige la próxima fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate proximaFecha;

    @Min(value = 0, message = "Los días de aviso no pueden ser negativos.")
    @Max(value = 30, message = "Máximo 30 días de aviso.")
    private int diasAviso = 3;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public Long getIdCajitaOrigen() { return idCajitaOrigen; }
    public void setIdCajitaOrigen(Long idCajitaOrigen) { this.idCajitaOrigen = idCajitaOrigen; }

    public Periodicidad getPeriodicidad() { return periodicidad; }
    public void setPeriodicidad(Periodicidad periodicidad) { this.periodicidad = periodicidad; }

    public LocalDate getProximaFecha() { return proximaFecha; }
    public void setProximaFecha(LocalDate proximaFecha) { this.proximaFecha = proximaFecha; }

    public int getDiasAviso() { return diasAviso; }
    public void setDiasAviso(int diasAviso) { this.diasAviso = diasAviso; }
}
