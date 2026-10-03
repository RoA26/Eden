package com.eden.persistencia;

import com.eden.modelo.Meta;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMetaRepositorio implements MetaRepositorio {

    private static final String SELECT_DETALLE = """
            SELECT mt.id, mt.id_usuario, mt.nombre, mt.monto_objetivo, mt.fecha_objetivo, mt.id_cajita, mt.activa,
                   c.nombre AS nombre_cajita,
            """ + JdbcCajitaRepositorio.EXPRESION_SALDO + """
             AS saldo_cajita
            FROM meta mt
            JOIN cajita c ON c.id = mt.id_cajita
            LEFT JOIN movimiento m ON m.id_cajita = c.id
            """;

    private static final String AGRUPAR = " GROUP BY mt.id, c.nombre ";

    private static final RowMapper<Meta> MAPEADOR = (rs, fila) -> {
        Meta meta = new Meta();
        meta.setId(rs.getLong("id"));
        meta.setIdUsuario(rs.getLong("id_usuario"));
        meta.setNombre(rs.getString("nombre"));
        meta.setMontoObjetivo(rs.getBigDecimal("monto_objetivo"));
        meta.setFechaObjetivo(rs.getObject("fecha_objetivo", LocalDate.class));
        meta.setIdCajita(rs.getLong("id_cajita"));
        meta.setActiva(rs.getBoolean("activa"));
        meta.setNombreCajita(rs.getString("nombre_cajita"));
        meta.setSaldoCajita(rs.getBigDecimal("saldo_cajita"));
        return meta;
    };

    private final JdbcClient jdbc;

    public JdbcMetaRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Meta meta) {
        return jdbc.sql("""
                        INSERT INTO meta (id_usuario, nombre, monto_objetivo, fecha_objetivo, id_cajita)
                        VALUES (:u, :nombre, :objetivo, :fecha, :cajita)
                        RETURNING id
                        """)
                .param("u", meta.getIdUsuario())
                .param("nombre", meta.getNombre())
                .param("objetivo", meta.getMontoObjetivo())
                .param("fecha", meta.getFechaObjetivo())
                .param("cajita", meta.getIdCajita())
                .query(Long.class).single();
    }

    @Override
    public void actualizar(Meta meta) {
        jdbc.sql("""
                        UPDATE meta SET nombre = :nombre, monto_objetivo = :objetivo, fecha_objetivo = :fecha,
                                        id_cajita = :cajita
                        WHERE id = :id AND id_usuario = :u
                        """)
                .param("nombre", meta.getNombre())
                .param("objetivo", meta.getMontoObjetivo())
                .param("fecha", meta.getFechaObjetivo())
                .param("cajita", meta.getIdCajita())
                .param("id", meta.getId())
                .param("u", meta.getIdUsuario())
                .update();
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long id, boolean activa) {
        jdbc.sql("UPDATE meta SET activa = :activa WHERE id = :id AND id_usuario = :u")
                .param("activa", activa).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public void eliminar(Long idUsuario, Long id) {
        jdbc.sql("DELETE FROM meta WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<Meta> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(SELECT_DETALLE + "WHERE mt.id = :id AND mt.id_usuario = :u" + AGRUPAR)
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<Meta> listar(Long idUsuario) {
        return jdbc.sql(SELECT_DETALLE + "WHERE mt.id_usuario = :u" + AGRUPAR
                        + "ORDER BY mt.activa DESC, mt.fecha_objetivo NULLS LAST, mt.nombre")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public boolean existeNombre(Long idUsuario, String nombre, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM meta
                                       WHERE id_usuario = :u AND lower(nombre) = lower(:nombre)
                                         AND id <> COALESCE(:excluido, -1))
                        """)
                .param("u", idUsuario).param("nombre", nombre).param("excluido", idExcluido)
                .query(Boolean.class).single();
    }
}
