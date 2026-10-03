package com.eden.persistencia;

import com.eden.modelo.Usuario;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Acceso a datos de usuarios con JdbcClient. Las excepciones de base de
 * datos (DataAccessException) no se capturan aqui: se propagan para que
 * ningun fallo quede silencioso.
 */
@Repository
public class JdbcUsuarioRepositorio implements UsuarioRepositorio {

    private static final RowMapper<Usuario> MAPEADOR = (rs, fila) -> {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setContrasenaHash(rs.getString("contrasena_hash"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setFechaRegistro(rs.getObject("fecha_registro", OffsetDateTime.class));
        return usuario;
    };

    private final JdbcClient jdbc;

    public JdbcUsuarioRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return jdbc.sql("""
                        SELECT id, nombre_usuario, nombre_completo, contrasena_hash, activo, fecha_registro
                        FROM usuario
                        WHERE nombre_usuario = :nombreUsuario
                        """)
                .param("nombreUsuario", nombreUsuario)
                .query(MAPEADOR)
                .optional();
    }

    @Override
    public boolean existeNombreUsuario(String nombreUsuario) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM usuario WHERE nombre_usuario = :nombreUsuario)")
                .param("nombreUsuario", nombreUsuario)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Long insertar(Usuario usuario) {
        return jdbc.sql("""
                        INSERT INTO usuario (nombre_usuario, nombre_completo, contrasena_hash)
                        VALUES (:nombreUsuario, :nombreCompleto, :contrasenaHash)
                        RETURNING id
                        """)
                .param("nombreUsuario", usuario.getNombreUsuario())
                .param("nombreCompleto", usuario.getNombreCompleto())
                .param("contrasenaHash", usuario.getContrasenaHash())
                .query(Long.class)
                .single();
    }

    @Override
    public void actualizarContrasena(Long idUsuario, String contrasenaHash) {
        int filas = jdbc.sql("UPDATE usuario SET contrasena_hash = :hash WHERE id = :id")
                .param("hash", contrasenaHash)
                .param("id", idUsuario)
                .update();
        if (filas != 1) {
            throw new IllegalStateException("No se actualizó la contraseña del usuario id=" + idUsuario);
        }
    }
}
