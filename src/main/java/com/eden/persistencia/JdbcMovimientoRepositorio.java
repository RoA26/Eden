package com.eden.persistencia;

import com.eden.dto.FiltroMovimientos;
import com.eden.dto.PaginaMovimientos;
import com.eden.dto.ResumenFuente;
import com.eden.dto.ResumenPeriodo;
import com.eden.dto.TotalPorCategoria;
import com.eden.dto.TotalPorDia;
import com.eden.dto.TotalPorMes;
import com.eden.modelo.Movimiento;
import com.eden.modelo.TipoCategoria;
import com.eden.modelo.TipoMovimiento;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcMovimientoRepositorio implements MovimientoRepositorio {

    private static final String SELECT_DETALLE = """
            SELECT m.id, m.id_usuario, m.tipo, m.fecha, m.monto, m.id_categoria, m.id_fuente, m.id_cajita,
                   m.id_relacionado, m.descripcion,
                   c.nombre AS nombre_categoria, c.tipo AS tipo_categoria,
                   f.nombre AS nombre_fuente, cj.nombre AS nombre_cajita, cr.nombre AS nombre_cajita_origen
            FROM movimiento m
            LEFT JOIN categoria c       ON c.id = m.id_categoria
            LEFT JOIN fuente_ingreso f  ON f.id = m.id_fuente
            LEFT JOIN cajita cj         ON cj.id = m.id_cajita
            LEFT JOIN movimiento r      ON r.id_relacionado = m.id
            LEFT JOIN cajita cr         ON cr.id = r.id_cajita
            """;

    private static final RowMapper<Movimiento> MAPEADOR = (rs, fila) -> {
        Movimiento m = new Movimiento();
        m.setId(rs.getLong("id"));
        m.setIdUsuario(rs.getLong("id_usuario"));
        m.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        m.setFecha(rs.getObject("fecha", LocalDate.class));
        m.setMonto(rs.getBigDecimal("monto"));
        m.setIdCategoria(rs.getObject("id_categoria", Long.class));
        m.setIdFuente(rs.getObject("id_fuente", Long.class));
        m.setIdCajita(rs.getObject("id_cajita", Long.class));
        m.setIdRelacionado(rs.getObject("id_relacionado", Long.class));
        m.setDescripcion(rs.getString("descripcion"));
        m.setNombreCategoria(rs.getString("nombre_categoria"));
        String tipoCategoria = rs.getString("tipo_categoria");
        m.setTipoCategoria(tipoCategoria == null ? null : TipoCategoria.valueOf(tipoCategoria));
        m.setNombreFuente(rs.getString("nombre_fuente"));
        m.setNombreCajita(rs.getString("nombre_cajita"));
        m.setNombreCajitaOrigen(rs.getString("nombre_cajita_origen"));
        return m;
    };

    private final JdbcClient jdbc;

    public JdbcMovimientoRepositorio(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertar(Movimiento m) {
        return jdbc.sql("""
                        INSERT INTO movimiento (id_usuario, tipo, fecha, monto, id_categoria, id_fuente,
                                                id_cajita, id_relacionado, descripcion)
                        VALUES (:u, :tipo, :fecha, :monto, :categoria, :fuente, :cajita, :relacionado, :descripcion)
                        RETURNING id
                        """)
                .param("u", m.getIdUsuario())
                .param("tipo", m.getTipo().name())
                .param("fecha", m.getFecha())
                .param("monto", m.getMonto())
                .param("categoria", m.getIdCategoria())
                .param("fuente", m.getIdFuente())
                .param("cajita", m.getIdCajita())
                .param("relacionado", m.getIdRelacionado())
                .param("descripcion", m.getDescripcion())
                .query(Long.class).single();
    }

    @Override
    public void actualizar(Movimiento m) {
        jdbc.sql("""
                        UPDATE movimiento SET fecha = :fecha, monto = :monto, id_categoria = :categoria,
                                              id_fuente = :fuente, descripcion = :descripcion
                        WHERE id = :id AND id_usuario = :u
                        """)
                .param("fecha", m.getFecha())
                .param("monto", m.getMonto())
                .param("categoria", m.getIdCategoria())
                .param("fuente", m.getIdFuente())
                .param("descripcion", m.getDescripcion())
                .param("id", m.getId())
                .param("u", m.getIdUsuario())
                .update();
    }

    @Override
    public void actualizarRelacionado(Long idUsuario, Long idOrigen, LocalDate fecha, BigDecimal monto) {
        jdbc.sql("UPDATE movimiento SET fecha = :fecha, monto = :monto WHERE id_relacionado = :origen AND id_usuario = :u")
                .param("fecha", fecha).param("monto", monto).param("origen", idOrigen).param("u", idUsuario)
                .update();
    }

    @Override
    public void eliminar(Long idUsuario, Long id) {
        jdbc.sql("DELETE FROM movimiento WHERE id = :id AND id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .update();
    }

    @Override
    public Optional<Movimiento> buscarPorId(Long idUsuario, Long id) {
        return jdbc.sql(SELECT_DETALLE + "WHERE m.id = :id AND m.id_usuario = :u")
                .param("id", id).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public Optional<Movimiento> buscarRelacionado(Long idUsuario, Long idOrigen) {
        return jdbc.sql(SELECT_DETALLE + "WHERE m.id_relacionado = :origen AND m.id_usuario = :u")
                .param("origen", idOrigen).param("u", idUsuario)
                .query(MAPEADOR).optional();
    }

    @Override
    public PaginaMovimientos buscar(Long idUsuario, FiltroMovimientos filtro, int tamanoPagina) {
        // Los retiros que pagan un gasto no se listan aparte: se muestran como "pagado desde" en el gasto.
        StringBuilder where = new StringBuilder(" WHERE m.id_usuario = :u AND m.id_relacionado IS NULL");
        Map<String, Object> params = new HashMap<>();
        params.put("u", idUsuario);

        if (filtro.getDesde() != null) {
            where.append(" AND m.fecha >= :desde");
            params.put("desde", filtro.getDesde());
        }
        if (filtro.getHasta() != null) {
            where.append(" AND m.fecha <= :hasta");
            params.put("hasta", filtro.getHasta());
        }
        if (filtro.getTipo() != null) {
            where.append(" AND m.tipo = :tipo");
            params.put("tipo", filtro.getTipo().name());
        }
        if (filtro.getIdCategoria() != null) {
            where.append(" AND m.id_categoria = :categoria");
            params.put("categoria", filtro.getIdCategoria());
        }
        if (filtro.getIdFuente() != null) {
            where.append(" AND m.id_fuente = :fuente");
            params.put("fuente", filtro.getIdFuente());
        }

        Map<String, Object> totales = jdbc.sql("""
                        SELECT COUNT(*) AS registros,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'INGRESO'), 0) AS ingresos,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'GASTO'), 0) AS gastos
                        FROM movimiento m
                        """ + where)
                .params(params)
                .query((rs, fila) -> Map.<String, Object>of(
                        "registros", rs.getLong("registros"),
                        "ingresos", rs.getBigDecimal("ingresos"),
                        "gastos", rs.getBigDecimal("gastos")))
                .single();

        Map<String, Object> paramsPagina = new HashMap<>(params);
        paramsPagina.put("limite", tamanoPagina);
        paramsPagina.put("desplazamiento", (long) filtro.getPagina() * tamanoPagina);

        List<Movimiento> movimientos = jdbc.sql(SELECT_DETALLE + where
                        + " ORDER BY m.fecha DESC, m.id DESC LIMIT :limite OFFSET :desplazamiento")
                .params(paramsPagina)
                .query(MAPEADOR).list();

        return new PaginaMovimientos(movimientos, filtro.getPagina(), tamanoPagina,
                (Long) totales.get("registros"),
                (BigDecimal) totales.get("ingresos"),
                (BigDecimal) totales.get("gastos"));
    }

    @Override
    public List<Movimiento> ultimos(Long idUsuario, int cantidad) {
        return jdbc.sql(SELECT_DETALLE + "WHERE m.id_usuario = :u AND m.id_relacionado IS NULL "
                        + "ORDER BY m.fecha DESC, m.id DESC LIMIT :cantidad")
                .param("u", idUsuario).param("cantidad", cantidad)
                .query(MAPEADOR).list();
    }

    @Override
    public List<Movimiento> listarPorCajita(Long idUsuario, Long idCajita, int cantidad) {
        return jdbc.sql(SELECT_DETALLE + "WHERE m.id_usuario = :u AND m.id_cajita = :c "
                        + "ORDER BY m.fecha DESC, m.id DESC LIMIT :cantidad")
                .param("u", idUsuario).param("c", idCajita).param("cantidad", cantidad)
                .query(MAPEADOR).list();
    }

    @Override
    public BigDecimal disponible(Long idUsuario) {
        return jdbc.sql("""
                        SELECT COALESCE(SUM(CASE WHEN tipo IN ('INGRESO', 'RETIRO') THEN monto
                                                 WHEN tipo IN ('GASTO', 'APORTE') THEN -monto
                                                 ELSE 0 END), 0)
                        FROM movimiento WHERE id_usuario = :u
                        """)
                .param("u", idUsuario)
                .query(BigDecimal.class).single();
    }

    @Override
    public ResumenPeriodo resumen(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'INGRESO'), 0) AS ingresos,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'GASTO' AND c.tipo = 'GASTO'), 0) AS personales,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'GASTO' AND c.tipo = 'COSTO_OPERATIVO'), 0) AS costos
                        FROM movimiento m
                        LEFT JOIN categoria c ON c.id = m.id_categoria
                        WHERE m.id_usuario = :u AND m.fecha BETWEEN :desde AND :hasta
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new ResumenPeriodo(
                        rs.getBigDecimal("ingresos"), rs.getBigDecimal("personales"), rs.getBigDecimal("costos")))
                .single();
    }

    @Override
    public List<TotalPorDia> totalesPorDia(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT fecha,
                               COALESCE(SUM(monto) FILTER (WHERE tipo = 'INGRESO'), 0) AS ingresos,
                               COALESCE(SUM(monto) FILTER (WHERE tipo = 'GASTO'), 0) AS gastos,
                               COALESCE(SUM(monto) FILTER (WHERE tipo = 'INGRESO' AND id_fuente IS NOT NULL), 0) AS producido
                        FROM movimiento
                        WHERE id_usuario = :u AND tipo IN ('INGRESO', 'GASTO') AND fecha BETWEEN :desde AND :hasta
                        GROUP BY fecha
                        ORDER BY fecha
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new TotalPorDia(rs.getObject("fecha", LocalDate.class),
                        rs.getBigDecimal("ingresos"), rs.getBigDecimal("gastos"), rs.getBigDecimal("producido")))
                .list();
    }

    @Override
    public List<TotalPorMes> totalesPorMes(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT to_char(fecha, 'YYYY-MM') AS mes,
                               COALESCE(SUM(monto) FILTER (WHERE tipo = 'INGRESO'), 0) AS ingresos,
                               COALESCE(SUM(monto) FILTER (WHERE tipo = 'GASTO'), 0) AS gastos
                        FROM movimiento
                        WHERE id_usuario = :u AND tipo IN ('INGRESO', 'GASTO') AND fecha BETWEEN :desde AND :hasta
                        GROUP BY 1
                        ORDER BY 1
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new TotalPorMes(YearMonth.parse(rs.getString("mes")),
                        rs.getBigDecimal("ingresos"), rs.getBigDecimal("gastos")))
                .list();
    }

    @Override
    public List<TotalPorCategoria> gastosPorCategoria(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT c.nombre AS categoria, SUM(m.monto) AS total
                        FROM movimiento m
                        JOIN categoria c ON c.id = m.id_categoria
                        WHERE m.id_usuario = :u AND m.tipo = 'GASTO' AND m.fecha BETWEEN :desde AND :hasta
                        GROUP BY c.nombre
                        ORDER BY total DESC
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new TotalPorCategoria(rs.getString("categoria"), rs.getBigDecimal("total")))
                .list();
    }

    @Override
    public List<ResumenFuente> resumenPorFuente(Long idUsuario, LocalDate desde, LocalDate hasta) {
        return jdbc.sql("""
                        SELECT f.id, f.nombre,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'INGRESO'), 0) AS bruto,
                               COALESCE(SUM(m.monto) FILTER (WHERE m.tipo = 'GASTO'), 0) AS costos,
                               COUNT(DISTINCT m.fecha) FILTER (WHERE m.tipo = 'INGRESO') AS dias
                        FROM fuente_ingreso f
                        LEFT JOIN movimiento m ON m.id_fuente = f.id AND m.fecha BETWEEN :desde AND :hasta
                        WHERE f.id_usuario = :u AND f.activa
                        GROUP BY f.id, f.nombre
                        ORDER BY f.nombre
                        """)
                .param("u", idUsuario).param("desde", desde).param("hasta", hasta)
                .query((rs, fila) -> new ResumenFuente(rs.getLong("id"), rs.getString("nombre"),
                        rs.getBigDecimal("bruto"), rs.getBigDecimal("costos"), rs.getInt("dias")))
                .list();
    }
}
