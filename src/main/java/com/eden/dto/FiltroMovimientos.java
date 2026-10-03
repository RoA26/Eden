package com.eden.dto;

import com.eden.modelo.TipoMovimiento;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Filtros del listado de movimientos (llegan por parametros GET). */
public class FiltroMovimientos {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate desde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hasta;

    private TipoMovimiento tipo;
    private Long idCategoria;
    private Long idFuente;
    private int pagina = 0;

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }

    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdFuente() { return idFuente; }
    public void setIdFuente(Long idFuente) { this.idFuente = idFuente; }

    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = Math.max(pagina, 0); }
}
