package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.ResumenPeriodo;
import com.eden.dto.ResumenSaldo;
import com.eden.dto.Tablero;
import com.eden.dto.TotalPorCategoria;
import com.eden.dto.TotalPorDia;
import com.eden.dto.TotalPorMes;
import com.eden.modelo.Cajita;
import com.eden.persistencia.MovimientoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Arma los datos del tablero de inicio: resumen del mes, cajitas, fuentes y graficas. */
@Service
public class TableroService {

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final int MESES_EN_GRAFICA = 6;

    private final MovimientoRepositorio movimientoRepositorio;
    private final CajitaService cajitaService;
    private final RecurrenteService recurrenteService;
    private final PresupuestoService presupuestoService;
    private final MetaService metaService;
    private final Clock reloj;

    public TableroService(MovimientoRepositorio movimientoRepositorio, CajitaService cajitaService,
                          RecurrenteService recurrenteService, PresupuestoService presupuestoService,
                          MetaService metaService, Clock reloj) {
        this.movimientoRepositorio = movimientoRepositorio;
        this.cajitaService = cajitaService;
        this.recurrenteService = recurrenteService;
        this.presupuestoService = presupuestoService;
        this.metaService = metaService;
        this.reloj = reloj;
    }

    public Tablero construir(Long idUsuario) {
        LocalDate hoy = LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
        YearMonth mes = YearMonth.from(hoy);
        YearMonth mesAnterior = mes.minusMonths(1);

        ResumenPeriodo resumenMes = movimientoRepositorio.resumen(idUsuario, mes.atDay(1), mes.atEndOfMonth());
        ResumenPeriodo resumenAnterior = movimientoRepositorio.resumen(idUsuario, mesAnterior.atDay(1), mesAnterior.atEndOfMonth());

        List<Cajita> cajitas = cajitaService.listarActivas(idUsuario);
        BigDecimal totalCajitas = cajitas.stream().map(Cajita::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Tablero(
                hoy,
                nombreMes(mes),
                movimientoRepositorio.disponible(idUsuario),
                resumenMes,
                resumenAnterior,
                cajitas,
                totalCajitas,
                movimientoRepositorio.resumenPorFuente(idUsuario, mes.atDay(1), mes.atEndOfMonth()),
                movimientoRepositorio.ultimos(idUsuario, 5),
                datosGraficas(idUsuario, mes, hoy),
                recurrenteService.pendientes(idUsuario),
                presupuestoService.alertasDelMes(idUsuario),
                metaService.listarActivas(idUsuario));
    }

    /** Saldo actual y resumen del mes; se devuelve tras cada registro hecho sin recargar la pagina. */
    public ResumenSaldo saldo(Long idUsuario) {
        YearMonth mes = YearMonth.from(LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA)));
        BigDecimal enCajitas = cajitaService.listarActivas(idUsuario).stream()
                .map(Cajita::getSaldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenSaldo(movimientoRepositorio.disponible(idUsuario), enCajitas,
                movimientoRepositorio.resumen(idUsuario, mes.atDay(1), mes.atEndOfMonth()));
    }

    /** Datos listos para Chart.js; la plantilla los serializa como JSON. */
    private Map<String, Object> datosGraficas(Long idUsuario, YearMonth mes, LocalDate hoy) {
        Map<String, Object> datos = new LinkedHashMap<>();

        // Ingresos vs gastos de los ultimos meses (se rellenan con cero los meses sin movimientos)
        YearMonth primerMes = mes.minusMonths(MESES_EN_GRAFICA - 1L);
        Map<YearMonth, TotalPorMes> porMes = movimientoRepositorio
                .totalesPorMes(idUsuario, primerMes.atDay(1), mes.atEndOfMonth()).stream()
                .collect(Collectors.toMap(TotalPorMes::mes, Function.identity()));
        List<String> meses = new ArrayList<>();
        List<BigDecimal> ingresos = new ArrayList<>();
        List<BigDecimal> gastos = new ArrayList<>();
        for (YearMonth m = primerMes; !m.isAfter(mes); m = m.plusMonths(1)) {
            TotalPorMes total = porMes.get(m);
            meses.add(m.getMonth().getDisplayName(TextStyle.SHORT, ES_CO));
            ingresos.add(total == null ? BigDecimal.ZERO : total.ingresos());
            gastos.add(total == null ? BigDecimal.ZERO : total.gastos());
        }
        datos.put("meses", meses);
        datos.put("ingresosPorMes", ingresos);
        datos.put("gastosPorMes", gastos);

        // Gastos del mes por categoria
        List<TotalPorCategoria> categorias = movimientoRepositorio
                .gastosPorCategoria(idUsuario, mes.atDay(1), mes.atEndOfMonth());
        datos.put("categorias", categorias.stream().map(TotalPorCategoria::categoria).toList());
        datos.put("gastosPorCategoria", categorias.stream().map(TotalPorCategoria::total).toList());

        // Producido diario del mes hasta hoy
        Map<LocalDate, BigDecimal> producidoPorDia = movimientoRepositorio
                .totalesPorDia(idUsuario, mes.atDay(1), hoy).stream()
                .collect(Collectors.toMap(TotalPorDia::fecha, TotalPorDia::producido));
        List<Integer> dias = new ArrayList<>();
        List<BigDecimal> producido = new ArrayList<>();
        for (LocalDate d = mes.atDay(1); !d.isAfter(hoy); d = d.plusDays(1)) {
            dias.add(d.getDayOfMonth());
            producido.add(producidoPorDia.getOrDefault(d, BigDecimal.ZERO));
        }
        datos.put("dias", dias);
        datos.put("producidoPorDia", producido);
        return datos;
    }

    private static String nombreMes(YearMonth mes) {
        return mes.getMonth().getDisplayName(TextStyle.FULL, ES_CO) + " " + mes.getYear();
    }
}
