package com.eden.persistencia;

import com.eden.modelo.RecuperacionContrasena;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Las fechas se calculan con now() de PostgreSQL: una sola fuente de tiempo para crear y validar. */
@Repository
public class JdbcRecuperacionContrasenaRepositorio implements RecuperacionContrasenaRepositorio {

    private final JdbcClient jdbc;

    public JdbcRecuperacionContrasenaRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertar(Long idUsuario, String pinHash, int minutosVigencia) {
        jdbc.sql("""
                        INSERT INTO recuperacion_contrasena (id_usuario, pin_hash, expira_en)
                        VALUES (:u, :hash, now() + (:minutos * INTERVAL '1 minute'))
                        """)
                .param("u", idUsuario).param("hash", pinHash).param("minutos", minutosVigencia)
                .update();
    }

    @Override
    public boolean existeSolicitudReciente(Long idUsuario, int segundos) {
        return jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM recuperacion_contrasena
                                       WHERE id_usuario = :u
                                         AND fecha_creacion > now() - (:segundos * INTERVAL '1 second'))
                        """)
                .param("u", idUsuario).param("segundos", segundos)
                .query(Boolean.class).single();
    }

    @Override
    public void anularPendientes(Long idUsuario) {
        jdbc.sql("UPDATE recuperacion_contrasena SET usada = TRUE WHERE id_usuario = :u AND NOT usada")
                .param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<RecuperacionContrasena> buscarVigenteParaActualizar(Long idUsuario, int maximoIntentos) {
        return jdbc.sql("""
                        SELECT id, id_usuario, pin_hash, intentos
                        FROM recuperacion_contrasena
                        WHERE id_usuario = :u AND NOT usada AND expira_en > now() AND intentos < :maximo
                        ORDER BY fecha_creacion DESC
                        LIMIT 1
                        FOR UPDATE
                        """)
                .param("u", idUsuario).param("maximo", maximoIntentos)
                .query((rs, fila) -> new RecuperacionContrasena(
                        rs.getLong("id"), rs.getLong("id_usuario"), rs.getString("pin_hash"), rs.getInt("intentos")))
                .optional();
    }

    @Override
    public int registrarIntentoFallido(Long idSolicitud) {
        return jdbc.sql("UPDATE recuperacion_contrasena SET intentos = intentos + 1 WHERE id = :id RETURNING intentos")
                .param("id", idSolicitud)
                .query(Integer.class).single();
    }

    @Override
    public void marcarUsada(Long idSolicitud) {
        jdbc.sql("UPDATE recuperacion_contrasena SET usada = TRUE WHERE id = :id")
                .param("id", idSolicitud)
                .update();
    }
}
