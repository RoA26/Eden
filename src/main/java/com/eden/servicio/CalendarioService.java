package com.eden.servicio;

import com.eden.config.RelojConfig;
import com.eden.dto.DiaCalendario;
import com.eden.dto.MesCalendario;
import com.eden.dto.TotalPorDia;
import com.eden.modelo.Frecuencia;
import com.eden.modelo.FuenteIngreso;
import com.eden.persistencia.MovimientoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calendario mensual de ingresos. Marca los dias en que entro dinero y,
 * si hay una fuente diaria activa, los dias pasados sin producido.
 */
@Service
public class CalendarioService {

    private final MovimientoRepositorio movimientoRepositorio;
    private final FuenteIngresoService fuenteService;
    private final Clock reloj;

    public CalendarioService(MovimientoRepositorio movimientoRepositorio, FuenteIngresoService fuenteService, Clock reloj) {
        this.movimientoRepositorio = movimientoRepositorio;
        this.fuenteService = fuenteService;
        this.reloj = reloj;
    }

    public YearMonth mesActual() {
        return YearMonth.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
    }

    public MesCalendario construir(Long idUsuario, YearMonth mes) {
        LocalDate hoy = LocalDate.now(reloj.withZone(RelojConfig.ZONA_COLOMBIA));
        LocalDate inicio = mes.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fin = mes.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Map<LocalDate, TotalPorDia> totales = movimientoRepositorio.totalesPorDia(idUsuario, inicio, fin).stream()
                .collect(Collectors.toMap(TotalPorDia::fecha, Function.identity()));

        // Desde cuando se espera producido diario: la creacion de la primera fuente diaria activa.
        LocalDate inicioDiarias = fuenteService.listarActivas(idUsuario).stream()
                .filter(f -> f.getFrecuencia() == Frecuencia.DIARIA)
                .map(FuenteIngreso::getFechaCreacion)
                .min(LocalDate::compareTo)
                .orElse(null);

        List<List<DiaCalendario>> semanas = new ArrayList<>();
        List<DiaCalendario> semana = new ArrayList<>();
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalGastos = BigDecimal.ZERO;
        int diasConProducido = 0;
        int diasSinProducido = 0;

        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            boolean delMes = YearMonth.from(d).equals(mes);
            TotalPorDia total = totales.get(d);
            BigDecimal ingresos = total == null ? BigDecimal.ZERO : total.ingresos();
            BigDecimal gastos = total == null ? BigDecimal.ZERO : total.gastos();
            boolean conProducido = total != null && total.producido().signum() > 0;
            boolean sinProducido = delMes && !conProducido && inicioDiarias != null
                    && d.isBefore(hoy) && !d.isBefore(inicioDiarias);

            if (delMes) {
                totalIngresos = totalIngresos.add(ingresos);
                totalGastos = totalGastos.add(gastos);
                diasConProducido += conProducido ? 1 : 0;
                diasSinProducido += sinProducido ? 1 : 0;
            }
            semana.add(new DiaCalendario(d, delMes, d.equals(hoy), ingresos, gastos, conProducido, sinProducido));
            if (semana.size() == 7) {
                semanas.add(semana);
                semana = new ArrayList<>();
            }
        }
        return new MesCalendario(mes, semanas, totalIngresos, totalGastos, diasConProducido, diasSinProducido);
    }
}
