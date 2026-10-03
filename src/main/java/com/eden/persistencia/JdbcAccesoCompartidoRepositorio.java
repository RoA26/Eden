package com.eden.persistencia;

import com.eden.modelo.AccesoCompartido;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAccesoCompartidoRepositorio implements AccesoCompartidoRepositorio {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private static final String SELECT_DETALLE = """
            SELECT a.id, a.id_titular, t.nombre_completo AS titular, a.id_invitado,
                   i.nombre_usuario AS usuario_invitado, i.nombre_completo AS invitado, a.fecha_concesion
            FROM acceso_compartido a
            JOIN usuario t ON t.id = a.id_titular
            JOIN usuario i ON i.id = a.id_invitado
            """;

    private static final RowMapper<AccesoCompartido> MAPEADOR = (rs, fila) -> new AccesoCompartido(
            rs.getLong("id"),
            rs.getLong("id_titular"),
            rs.getString("titular"),
            rs.getLong("id_invitado"),
            rs.getString("usuario_invitado"),
            rs.getString("invitado"),
            rs.getObject("fecha_concesion", OffsetDateTime.class).atZoneSameInstant(ZONA).toLocalDate());

    private final JdbcClient jdbc;

    public JdbcAccesoCompartidoRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void conceder(Long idTitular, Long idInvitado) {
        jdbc.sql("INSERT INTO acceso_compartido (id_titular, id_invitado) VALUES (:t, :i) ON CONFLICT DO NOTHING")
                .param("t", idTitular).param("i", idInvitado)
                .update();
    }

    @Override
    public void revocar(Long idTitular, Long idAcceso) {
        jdbc.sql("DELETE FROM acceso_compartido WHERE id = :id AND id_titular = :t")
                .param("id", idAcceso).param("t", idTitular)
                .update();
    }

    @Override
    public List<AccesoCompartido> listarConcedidos(Long idTitular) {
        return jdbc.sql(SELECT_DETALLE + "WHERE a.id_titular = :t ORDER BY i.nombre_completo")
                .param("t", idTitular)
                .query(MAPEADOR).list();
    }

    @Override
    public List<AccesoCompartido> listarRecibidos(Long idInvitado) {
        return jdbc.sql(SELECT_DETALLE + "WHERE a.id_invitado = :i ORDER BY t.nombre_completo")
                .param("i", idInvitado)
                .query(MAPEADOR).list();
    }

    @Override
    public Optional<AccesoCompartido> buscar(Long idTitular, Long idInvitado) {
        return jdbc.sql(SELECT_DETALLE + "WHERE a.id_titular = :t AND a.id_invitado = :i")
                .param("t", idTitular).param("i", idInvitado)
                .query(MAPEADOR).optional();
    }
}
