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

    const css = getComputedStyle(document.documentElement);
    const color = (nombre, respaldo) => (css.getPropertyValue(nombre).trim() || respaldo);
    const colorIngreso = color('--ingreso', '#00FF7F');
    const colorGasto = color('--gasto', '#F43F5E');
    const colorTexto = color('--texto-suave', '#8aa69b');
    const colorLinea = color('--linea', 'rgba(74, 222, 128, 0.15)');

    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
    Chart.defaults.color = colorTexto;

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
                datasets: [{ label: 'Producido', data: datos.producidoPorDia, backgroundColor: colorIngreso, borderRadius: 3 }]
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
                    { label: 'Ingresos', data: datos.ingresosPorMes, backgroundColor: colorIngreso, borderRadius: 3 },
                    { label: 'Gastos', data: datos.gastosPorMes, backgroundColor: colorGasto, borderRadius: 3 }
                ]
            },
            options: { maintainAspectRatio: false, scales: ejes, plugins: { tooltip: tooltipPesos } }
        });
    }

    const categorias = document.getElementById('grafica-categorias');
    if (categorias && datos.categorias && datos.categorias.length) {
        const paleta = ['#00FF7F', '#22D3EE', '#A78BFA', '#F43F5E', '#FCD34D', '#FB923C', '#4ADE80', '#8aa69b'];
        new Chart(categorias, {
            type: 'doughnut',
            data: {
                labels: datos.categorias,
                datasets: [{ data: datos.gastosPorCategoria, backgroundColor: datos.categorias.map((_, i) => paleta[i % paleta.length]), borderWidth: 0 }]
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
