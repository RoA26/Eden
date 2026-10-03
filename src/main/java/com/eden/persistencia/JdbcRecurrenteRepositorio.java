package com.eden.persistencia;

import com.eden.modelo.Periodicidad;
import com.eden.modelo.Recurrente;
import com.eden.modelo.TipoMovimiento;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRecurrenteRepositorio implements RecurrenteRepositorio {

    private static final String SELECT_DETALLE = """
            SELECT r.id, r.id_usuario, r.nombre, r.tipo, r.monto, r.id_categoria, r.id_fuente, r.id_cajita_origen,
                   r.periodicidad, r.dia_ancla, r.proxima_fecha, r.dias_aviso, r.activo,
                   c.nombre AS nombre_categoria, f.nombre AS nombre_fuente, cj.nombre AS nombre_cajita
            FROM recurrente r
            JOIN categoria c           ON c.id = r.id_categoria
            LEFT JOIN fuente_ingreso f ON f.id = r.id_fuente
            LEFT JOIN cajita cj        ON cj.id = r.id_cajita_origen
            """;

    private static final RowMapper<Recurrente> MAPEADOR = (rs, fila) -> {
        Recurrente r = new Recurrente();
        r.setId(rs.getLong("id"));
        r.setIdUsuario(rs.getLong("id_usuario"));
        r.setNombre(rs.getString("nombre"));
        r.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        r.setMonto(rs.getBigDecimal("monto"));
        r.setIdCategoria(rs.getLong("id_categoria"));
        r.setIdFuente(rs.getObject("id_fuente", Long.class));
        r.setIdCajitaOrigen(rs.getObject("id_cajita_origen", Long.class));
        r.setPeriodicidad(Periodicidad.valueOf(rs.getString("periodicidad")));
        r.setDiaAncla(rs.getInt("dia_ancla"));
        r.setProximaFecha(rs.getObject("proxima_fecha", LocalDate.class));
        r.setDiasAviso(rs.getInt("dias_aviso"));
        r.setActivo(rs.getBoolean("activo"));
        r.setNombreCategoria(rs.getString("nombre_categoria"));
        r.setNombreFuente(rs.getString("nombre_fuente"));
        r.setNombreCajitaOrigen(rs.getString("nombre_cajita"));
        return r;
    };

    private final JdbcClient jdbc;

    public JdbcRecurrenteRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Recurrente r) {
        return jdbc.sql("""
                        INSERT INTO recurrente (id_usuario, nombre, tipo, monto, id_categoria, id_fuente, id_cajita_origen,
                                                periodicidad, dia_ancla, proxima_fecha, dias_aviso)
                        VALUES (:u, :nombre, :tipo, :monto, :categoria, :fuente, :cajita,
                                :periodicidad, :ancla, :proxima, :aviso)
                        RETURNING id
                        """)
                .param("u", r.getIdUsuario())
                .param("nombre", r.getNombre())
                .param("tipo", r.getTipo().name())
                .param("monto", r.getMonto())
                .param("categoria", r.getIdCategoria())
                .param("fuente", r.getIdFuente())
                .param("cajita", r.getIdCajitaOrigen())
                .param("periodicidad", r.getPeriodicidad().name())
                .param("ancla", r.getDiaAncla())
                .param("proxima", r.getProximaFecha())
                .param("aviso", r.getDiasAviso())
                .query(Long.class).single();
    }

    @Override
    public void actualizar(Recurrente r) {
        jdbc.sql("""
                        UPDATE recurrente SET nombre = :nombre, tipo = :tipo, monto = :monto, id_categoria = :categoria,
                               id_fuente = :fuente, id_cajita_origen = :cajita, periodicidad = :periodicidad,
                               dia_ancla = :ancla, proxima_fecha = :proxima, dias_aviso = :aviso
                        WHERE id = :id AND id_usuario = :u
                        """)
                .param("nombre", r.getNombre())
                .param("tipo", r.getTipo().name())
                .param("monto", r.getMonto())
                .param("categoria", r.getIdCategoria())
                .param("fuente", r.getIdFuente())
                .param("cajita", r.getIdCajitaOrigen())
                .param("periodicidad", r.getPeriodicidad().name())
                .param("ancla", r.getDiaAncla())
                .param("proxima", r.getProximaFecha())
                .param("aviso", r.getDiasAviso())
                .param("id", r.getId())
                .param("u", r.getIdUsuario())
                .update();
    }

    @Override
    public void actualizarProximaFecha(Long idUsuario, Long id, LocalDate proximaFecha) {
        jdbc.sql("UPDATE recurrente SET proxima_fecha = :proxima WHERE id = :id AND id_usuario = :u")
                .param("proxima", proximaFecha).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long id, boolean activo) {
        jdbc.sql("UPDATE recurrente SET activo = :activo WHERE id = :id AND id_usuario = :u")
                .param("activo", activo).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public void eliminar(Long idUsuario, Long id) {
        jdbc.sql("DELETE FROM recurrente WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<Recurrente> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(SELECT_DETALLE + "WHERE r.id = :id AND r.id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<Recurrente> listar(Long idUsuario) {
        return jdbc.sql(SELECT_DETALLE + "WHERE r.id_usuario = :u ORDER BY r.activo DESC, r.proxima_fecha, r.nombre")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public List<Recurrente> listarPendientes(Long idUsuario, LocalDate hoy) {
        return jdbc.sql(SELECT_DETALLE + """
                        WHERE r.id_usuario = :u AND r.activo AND r.proxima_fecha - r.dias_aviso <= :hoy
                        ORDER BY r.proxima_fecha, r.nombre
                        """)
                .param("u", idUsuario).param("hoy", hoy)
                .query(MAPEADOR).list();
    }

    @Override
    public boolean existeNombre(Long idUsuario, String nombre, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM recurrente
                                       WHERE id_usuario = :u AND lower(nombre) = lower(:nombre)
                                         AND id <> COALESCE(:excluido, -1))
                        """)
                .param("u", idUsuario).param("nombre", nombre).param("excluido", idExcluido)
                .query(Boolean.class).single();
    }
}
