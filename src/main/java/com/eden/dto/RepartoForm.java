package com.eden.dto;

import java.util.ArrayList;
import java.util.List;

/** Reparto confirmado por el usuario: un monto por cajita (puede ajustar la sugerencia). */
public class RepartoForm {

    private List<Linea> lineas = new ArrayList<>();

    public List<Linea> getLineas() { return lineas; }
    public void setLineas(List<Linea> lineas) { this.lineas = lineas; }

    public static class Linea {
        private Long idCajita;
        private String monto;

        public Linea() {
        }

        public Linea(Long idCajita, String monto) {
            this.idCajita = idCajita;
            this.monto = monto;
        }

        public Long getIdCajita() { return idCajita; }
        public void setIdCajita(Long idCajita) { this.idCajita = idCajita; }

        public String getMonto() { return monto; }
        public void setMonto(String monto) { this.monto = monto; }
    }
}
