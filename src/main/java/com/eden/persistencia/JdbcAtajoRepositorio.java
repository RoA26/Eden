package com.eden.persistencia;

import com.eden.modelo.Atajo;
import com.eden.modelo.TipoMovimiento;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAtajoRepositorio implements AtajoRepositorio {

    private static final String COLUMNAS = """
            SELECT a.id, a.id_usuario, a.nombre, a.tipo, a.monto, a.id_categoria, a.id_fuente,
                   c.nombre AS nombre_categoria
            FROM atajo a
            JOIN categoria c ON c.id = a.id_categoria
            """;

    private static final RowMapper<Atajo> MAPEADOR = (rs, fila) -> {
        Atajo a = new Atajo();
        a.setId(rs.getLong("id"));
        a.setIdUsuario(rs.getLong("id_usuario"));
        a.setNombre(rs.getString("nombre"));
        a.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        a.setMonto(rs.getBigDecimal("monto"));
        a.setIdCategoria(rs.getLong("id_categoria"));
        a.setIdFuente(rs.getObject("id_fuente", Long.class));
        a.setNombreCategoria(rs.getString("nombre_categoria"));
        return a;
    };

    private final JdbcClient jdbc;

    public JdbcAtajoRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Atajo atajo) {
        return jdbc.sql("""
                        INSERT INTO atajo (id_usuario, nombre, tipo, monto, id_categoria, id_fuente)
                        VALUES (:u, :nombre, :tipo, :monto, :categoria, :fuente) RETURNING id
                        """)
                .param("u", atajo.getIdUsuario())
                .param("nombre", atajo.getNombre())
                .param("tipo", atajo.getTipo().name())
                .param("monto", atajo.getMonto())
                .param("categoria", atajo.getIdCategoria())
                .param("fuente", atajo.getIdFuente())
                .query(Long.class)
                .single();
    }

    @Override
    public void eliminar(Long idUsuario, Long id) {
        jdbc.sql("DELETE FROM atajo WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<Atajo> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(COLUMNAS + "WHERE a.id = :id AND a.id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<Atajo> listar(Long idUsuario) {
        return jdbc.sql(COLUMNAS + "WHERE a.id_usuario = :u ORDER BY a.id")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public int contar(Long idUsuario) {
        return jdbc.sql("SELECT COUNT(*) FROM atajo WHERE id_usuario = :u")
                .param("u", idUsuario)
                .query(Integer.class).single();
    }

    @Override
    public boolean existeNombre(Long idUsuario, String nombre) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM atajo WHERE id_usuario = :u AND lower(nombre) = lower(:nombre))")
                .param("u", idUsuario).param("nombre", nombre)
                .query(Boolean.class).single();
    }
}
