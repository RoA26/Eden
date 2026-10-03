package com.eden.modelo;

public class Categoria {

    private Long id;
    private Long idUsuario;
    private String nombre;
    private TipoCategoria tipo;
    private boolean activa = true;

    public Categoria() {
    }

    public Categoria(Long idUsuario, String nombre, TipoCategoria tipo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
