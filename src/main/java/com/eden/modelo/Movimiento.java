package com.eden.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Movimiento {

    private Long id;
    private Long idUsuario;
    private TipoMovimiento tipo;
    private LocalDate fecha;
    private BigDecimal monto;
    private Long idCategoria;
    private Long idFuente;
    private Long idCajita;
    private Long idRelacionado;
    private String descripcion;

    // Datos descriptivos que llegan por JOIN (solo lectura)
    private String nombreCategoria;
    private TipoCategoria tipoCategoria;
    private String nombreFuente;
    private String nombreCajita;
    private String nombreCajitaOrigen;

    /** Solo ingresos y gastos se editan; las operaciones de cajita se eliminan y se registran de nuevo. */
    public boolean isEditable() {
        return tipo != null && tipo.esIngresoOGasto();
    }

    /** Un retiro que paga un gasto se elimina junto con ese gasto, no por separado. */
    public boolean isEliminable() {
        return idRelacionado == null;
    }

    public boolean isCostoOperativo() {
        return tipoCategoria == TipoCategoria.COSTO_OPERATIVO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public Long getIdCajita() { return idCajita; }
    public void setIdCajita(Long idCajita) { this.idCajita = idCajita; }

    public Long getIdRelacionado() { return idRelacionado; }
    public void setIdRelacionado(Long idRelacionado) { this.idRelacionado = idRelacionado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public TipoCategoria getTipoCategoria() { return tipoCategoria; }
    public void setTipoCategoria(TipoCategoria tipoCategoria) { this.tipoCategoria = tipoCategoria; }

    public String getNombreFuente() { return nombreFuente; }
    public void setNombreFuente(String nombreFuente) { this.nombreFuente = nombreFuente; }

    public String getNombreCajita() { return nombreCajita; }
    public void setNombreCajita(String nombreCajita) { this.nombreCajita = nombreCajita; }

    public String getNombreCajitaOrigen() { return nombreCajitaOrigen; }
    public void setNombreCajitaOrigen(String nombreCajitaOrigen) { this.nombreCajitaOrigen = nombreCajitaOrigen; }
}
