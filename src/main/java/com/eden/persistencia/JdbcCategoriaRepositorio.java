package com.eden.persistencia;

import com.eden.modelo.Categoria;
import com.eden.modelo.TipoCategoria;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCategoriaRepositorio implements CategoriaRepositorio {

    private static final String COLUMNAS = "SELECT id, id_usuario, nombre, tipo, activa FROM categoria ";

    private static final RowMapper<Categoria> MAPEADOR = (rs, fila) -> {
        Categoria c = new Categoria();
        c.setId(rs.getLong("id"));
        c.setIdUsuario(rs.getLong("id_usuario"));
        c.setNombre(rs.getString("nombre"));
        c.setTipo(TipoCategoria.valueOf(rs.getString("tipo")));
        c.setActiva(rs.getBoolean("activa"));
        return c;
    };

    private final JdbcClient jdbc;

    public JdbcCategoriaRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertarTodas(List<Categoria> categorias) {
        categorias.forEach(this::insertar);
    }

    @Override
    public Long insertar(Categoria categoria) {
        return jdbc.sql("INSERT INTO categoria (id_usuario, nombre, tipo) VALUES (:u, :nombre, :tipo) RETURNING id")
                .param("u", categoria.getIdUsuario())
                .param("nombre", categoria.getNombre())
                .param("tipo", categoria.getTipo().name())
                .query(Long.class)
                .single();
    }

    @Override
    public void actualizarNombre(Long idUsuario, Long id, String nombre) {
        jdbc.sql("UPDATE categoria SET nombre = :nombre WHERE id = :id AND id_usuario = :u")
                .param("nombre", nombre).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long id, boolean activa) {
        jdbc.sql("UPDATE categoria SET activa = :activa WHERE id = :id AND id_usuario = :u")
                .param("activa", activa).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<Categoria> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(COLUMNAS + "WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<Categoria> listar(Long idUsuario) {
        return jdbc.sql(COLUMNAS + "WHERE id_usuario = :u ORDER BY tipo, activa DESC, nombre")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public boolean existeNombre(Long idUsuario, TipoCategoria tipo, String nombre, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM categoria
                                       WHERE id_usuario = :u AND tipo = :tipo AND lower(nombre) = lower(:nombre)
                                         AND id <> COALESCE(:excluido, -1))
                        """)
                .param("u", idUsuario).param("tipo", tipo.name()).param("nombre", nombre).param("excluido", idExcluido)
                .query(Boolean.class).single();
    }
}
