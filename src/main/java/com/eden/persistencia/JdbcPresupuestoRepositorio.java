package com.eden.persistencia;

import com.eden.dto.EstadoPresupuesto;
import com.eden.modelo.TipoCategoria;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcPresupuestoRepositorio implements PresupuestoRepositorio {

    private final JdbcClient jdbc;

    public JdbcPresupuestoRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Long idUsuario, Long idCategoria, BigDecimal montoMensual) {
        jdbc.sql("""
                        INSERT INTO presupuesto (id_usuario, id_categoria, monto_mensual)
                        VALUES (:u, :categoria, :monto)
                        ON CONFLICT (id_usuario, id_categoria) DO UPDATE SET monto_mensual = EXCLUDED.monto_mensual
                        """)
                .param("u", idUsuario).param("categoria", idCategoria).param("monto", montoMensual)
                .update();
    }

    @Override
    public void eliminar(Long idUsuario, Long id) {
        jdbc.sql("DELETE FROM presupuesto WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public List<EstadoPresupuesto> listarConGastado(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT p.id, p.id_categoria, c.nombre, c.tipo, p.monto_mensual,
                               COALESCE(SUM(m.monto), 0) AS gastado
                        FROM presupuesto p
                        JOIN categoria c ON c.id = p.id_categoria
                        LEFT JOIN movimiento m ON m.id_categoria = p.id_categoria AND m.tipo = 'GASTO'
                                              AND m.fecha BETWEEN :desde AND :hasta
                        WHERE p.id_usuario = :u
                        GROUP BY p.id, c.nombre, c.tipo
                        ORDER BY c.tipo, c.nombre
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new EstadoPresupuesto(
                        rs.getLong("id"), rs.getLong("id_categoria"), rs.getString("nombre"),
                        TipoCategoria.valueOf(rs.getString("tipo")),
                        rs.getBigDecimal("monto_mensual"), rs.getBigDecimal("gastado")))
                .list();
    }
}
