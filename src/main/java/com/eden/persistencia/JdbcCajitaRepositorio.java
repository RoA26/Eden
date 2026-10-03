package com.eden.persistencia;

import com.eden.modelo.Cajita;
import com.eden.modelo.PropositoCajita;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCajitaRepositorio implements CajitaRepositorio {

    /** Saldo = aportes + rendimientos + saldo inicial - retiros (RN-06). */
    static final String EXPRESION_SALDO = """
            COALESCE(SUM(CASE WHEN m.tipo IN ('APORTE', 'RENDIMIENTO', 'SALDO_INICIAL') THEN m.monto
                              WHEN m.tipo = 'RETIRO' THEN -m.monto
                              ELSE 0 END), 0)
            """;

    private static final String SELECT_CON_SALDO = """
            SELECT c.id, c.id_usuario, c.nombre, c.proposito, c.porcentaje, c.es_resto, c.activa,
            """ + EXPRESION_SALDO + """
             AS saldo
            FROM cajita c
            LEFT JOIN movimiento m ON m.id_cajita = c.id
            """;

    private static final String AGRUPAR = " GROUP BY c.id ";

    private static final RowMapper<Cajita> MAPEADOR = (rs, fila) -> {
        Cajita c = new Cajita();
        c.setId(rs.getLong("id"));
        c.setIdUsuario(rs.getLong("id_usuario"));
        c.setNombre(rs.getString("nombre"));
        c.setProposito(PropositoCajita.valueOf(rs.getString("proposito")));
        c.setPorcentaje(rs.getBigDecimal("porcentaje"));
        c.setEsResto(rs.getBoolean("es_resto"));
        c.setActiva(rs.getBoolean("activa"));
        c.setSaldo(rs.getBigDecimal("saldo"));
        return c;
    };

    private final JdbcClient jdbc;

    public JdbcCajitaRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Cajita cajita) {
        return jdbc.sql("""
                        INSERT INTO cajita (id_usuario, nombre, proposito, porcentaje, es_resto)
                        VALUES (:u, :nombre, :proposito, :porcentaje, :esResto)
                        RETURNING id
                        """)
                .param("u", cajita.getIdUsuario())
                .param("nombre", cajita.getNombre())
                .param("proposito", cajita.getProposito().name())
                .param("porcentaje", cajita.getPorcentaje())
                .param("esResto", cajita.isEsResto())
                .query(Long.class).single();
    }

    @Override
    public void actualizar(Cajita cajita) {
        jdbc.sql("""
                        UPDATE cajita SET nombre = :nombre, proposito = :proposito,
                                          porcentaje = :porcentaje, es_resto = :esResto
                        WHERE id = :id AND id_usuario = :u
                        """)
                .param("nombre", cajita.getNombre())
                .param("proposito", cajita.getProposito().name())
                .param("porcentaje", cajita.getPorcentaje())
                .param("esResto", cajita.isEsResto())
                .param("id", cajita.getId())
                .param("u", cajita.getIdUsuario())
                .update();
    }

    @Override
    public void cambiarEstado(Long idUsuario, Long id, boolean activa) {
        jdbc.sql("UPDATE cajita SET activa = :activa WHERE id = :id AND id_usuario = :u")
                .param("activa", activa).param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public void desmarcarResto(Long idUsuario, Long idExcluido) {
        jdbc.sql("UPDATE cajita SET es_resto = FALSE WHERE id_usuario = :u AND id <> COALESCE(:excluido, -1)")
                .param("u", idUsuario).param("excluido", idExcluido)
                .update();
    }

    @Override
    public Optional<Cajita> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(SELECT_CON_SALDO + "WHERE c.id = :id AND c.id_usuario = :u" + AGRUPAR)
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public List<Cajita> listar(Long idUsuario) {
        return jdbc.sql(SELECT_CON_SALDO + "WHERE c.id_usuario = :u" + AGRUPAR
                        + "ORDER BY c.activa DESC, c.es_resto, c.porcentaje DESC, c.nombre")
                .param("u", idUsuario)
                .query(MAPEADOR).list();
    }

    @Override
    public boolean existeNombre(Long idUsuario, String nombre, Long idExcluido) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM cajita
                                       WHERE id_usuario = :u AND lower(nombre) = lower(:nombre)
                                         AND id <> COALESCE(:excluido, -1))
                        """)
                .param("u", idUsuario).param("nombre", nombre).param("excluido", idExcluido)
                .query(Boolean.class).single();
    }

    @Override
    public BigDecimal saldo(Long idUsuario, Long idCajita) {
        return jdbc.sql("SELECT " + EXPRESION_SALDO + " FROM movimiento m WHERE m.id_usuario = :u AND m.id_cajita = :c")
                .param("u", idUsuario).param("c", idCajita)
                .query(BigDecimal.class).single();
    }
}
