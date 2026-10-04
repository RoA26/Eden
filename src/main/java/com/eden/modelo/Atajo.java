package com.eden.modelo;

import java.math.BigDecimal;

/** Boton rapido del tablero: una plantilla de ingreso o gasto que se registra con un toque. */
public class Atajo {

    private Long id;
    private Long idUsuario;
    private String nombre;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private Long idCategoria;
    private Long idFuente;

    // Datos descriptivos que llegan por JOIN (solo lectura)
    private String nombreCategoria;

    public boolean esIngreso() {
        return tipo == TipoMovimiento.INGRESO;
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

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }
}
