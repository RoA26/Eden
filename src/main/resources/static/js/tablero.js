// Graficas del tablero. Los datos llegan como JSON en <script id="datos-graficas">.
(function () {
    const nodo = document.getElementById('datos-graficas');
    if (!nodo || typeof Chart === 'undefined') {
        return;
    }
    let datos;
    try {
        datos = JSON.parse(nodo.textContent);
    } catch (e) {
        console.error('Datos de graficas invalidos', e);
        return;
    }
    if (!datos) {
        return;
    }

    // Paleta "Cozy Fintech" (la misma de frontend/tailwind.config.js).
    const colorIngreso = '#10B981';
    const colorGasto = '#EF4444';
    const colorTexto = '#9DAAA2';
    const colorLinea = 'rgba(255, 255, 255, 0.05)';

    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = colorTexto;
    Chart.defaults.plugins.tooltip.backgroundColor = 'rgba(21, 26, 23, 0.95)';
    Chart.defaults.plugins.tooltip.borderColor = 'rgba(255, 255, 255, 0.10)';
    Chart.defaults.plugins.tooltip.borderWidth = 1;
    Chart.defaults.plugins.tooltip.padding = 10;
    Chart.defaults.plugins.tooltip.cornerRadius = 12;
    Chart.defaults.plugins.legend.labels.usePointStyle = true;
    Chart.defaults.plugins.legend.labels.boxWidth = 8;

    const pesos = (valor) => '$ ' + Number(valor).toLocaleString('es-CO', { maximumFractionDigits: 0 });
    const compacto = (valor) => {
        const n = Number(valor);
        if (n >= 1000000) return (n / 1000000).toLocaleString('es-CO', { maximumFractionDigits: 1 }) + 'M';
        if (n >= 1000) return Math.round(n / 1000) + 'k';
        return String(n);
    };

    const ejes = {
        x: { grid: { display: false } },
        y: { beginAtZero: true, grid: { color: colorLinea }, ticks: { callback: compacto } }
    };
    const tooltipPesos = {
        callbacks: { label: (ctx) => (ctx.dataset.label ? ctx.dataset.label + ': ' : '') + pesos(ctx.parsed.y ?? ctx.parsed) }
    };

    const producido = document.getElementById('grafica-producido');
    if (producido) {
        new Chart(producido, {
            type: 'bar',
            data: {
                labels: datos.dias,
                datasets: [{ label: 'Producido', data: datos.producidoPorDia, backgroundColor: colorIngreso, borderRadius: 6, maxBarThickness: 18 }]
            },
            options: { maintainAspectRatio: false, scales: ejes, plugins: { legend: { display: false }, tooltip: tooltipPesos } }
        });
    }

    const meses = document.getElementById('grafica-meses');
    if (meses) {
        new Chart(meses, {
            type: 'bar',
            data: {
                labels: datos.meses,
                datasets: [
                    { label: 'Ingresos', data: datos.ingresosPorMes, backgroundColor: colorIngreso, borderRadius: 6, maxBarThickness: 16 },
                    { label: 'Gastos', data: datos.gastosPorMes, backgroundColor: colorGasto, borderRadius: 6, maxBarThickness: 16 }
                ]
            },
            options: { maintainAspectRatio: false, scales: ejes, plugins: { tooltip: tooltipPesos } }
        });
    }

    const categorias = document.getElementById('grafica-categorias');
    if (categorias && datos.categorias && datos.categorias.length) {
        const paleta = ['#EF4444', '#F59E0B', '#10B981', '#38BDF8', '#A78BFA', '#F472B6', '#FB923C', '#9DAAA2'];
        new Chart(categorias, {
            type: 'doughnut',
            data: {
                labels: datos.categorias,
                datasets: [{ data: datos.gastosPorCategoria, backgroundColor: datos.categorias.map((_, i) => paleta[i % paleta.length]), borderWidth: 2, borderColor: '#151A17', hoverOffset: 6 }]
            },
            options: {
                maintainAspectRatio: false,
                cutout: '62%',
                plugins: {
                    legend: { position: 'bottom' },
                    tooltip: { callbacks: { label: (ctx) => ctx.label + ': ' + pesos(ctx.parsed) } }
                }
            }
        });
    }
})();
