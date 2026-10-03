package com.eden.persistencia;

import com.eden.modelo.Frecuencia;
import com.eden.modelo.FuenteIngreso;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcFuenteIngresoRepositorio implements FuenteIngresoRepositorio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final String COLUMNAS =
            "SELECT id, id_usuario, nombre, frecuencia, activa, fecha_creacion FROM fuente_ingreso ";

    private static final RowMapper<FuenteIngreso> MAPEADOR = (rs, fila) -> {
        FuenteIngreso f = new FuenteIngreso();
        f.setId(rs.getLong("id"));
        f.setIdUsuario(rs.getLong("id_usuario"));
        f.setNombre(rs.getString("nombre"));
        f.setFrecuencia(Frecuencia.valueOf(rs.getString("frecuencia")));
        f.setActiva(rs.getBoolean("activa"));
        f.setFechaCreacion(rs.getObject("fecha_creacion", java.time.OffsetDateTime.class)
                .atZoneSameInstant(ZONA).toLocalDate());
        return f;
    };

    private final JdbcClient jdbc;

    public JdbcFuenteIngresoRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(FuenteIngreso fuente) {
        return jdbc.sql("INSERT INTO fuente_ingreso (id_usuario, nombre, frecuencia) VALUES (:u, :nombre, :frecuencia) RETURNING id")
                .param("u", fuente.getIdUsuario())
                .param("nombre", fuente.getNombre())
                .param("frecuencia", fuente.getFrecuencia().name())
                .query(Long.class).single();
    }

    @Override
    public void actualizar(FuenteIngreso fuente) {
        jdbc.sql("UPDATE fuente_ingreso SET nombre = :nombre, frecuencia = :frecuencia WHERE id = :id AND id_usuario = :u")
                .param("nombre", fuente.getNombre())
                .param("frecuencia", fuente.getFrecuencia().name())
                .param("id", fuente.getId())
                .param("u", fuente.getIdUsuario())
                .update();
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long id, boolean activa) {
        jdbc.sql("UPDATE fuente_ingreso SET activa = :activa WHERE id = :id AND id_usuario = :u")
                .param("activa", activa).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<FuenteIngreso> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(COLUMNAS + "WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<FuenteIngreso> listar(Long idUsuario) {
        return jdbc.sql(COLUMNAS + "WHERE id_usuario = :u ORDER BY activa DESC, nombre")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public boolean existeNombre(Long idUsuario, String nombre, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM fuente_ingreso
                                       WHERE id_usuario = :u AND lower(nombre) = lower(:nombre)
                                         AND id <> COALESCE(:excluido, -1))
                        """)
                .param("u", idUsuario).param("nombre", nombre).param("excluido", idExcluido)
                .query(Boolean.class).single();
    }
}
