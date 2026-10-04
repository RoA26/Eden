// Service worker de Eden.
// Politica: los archivos estaticos (CSS, JS, iconos) se guardan en cache para
// que la app abra rapido. Las paginas con datos financieros NUNCA se guardan:
// siempre se piden al servidor y, si no hay conexion, se muestra offline.html.
const VERSION = 'eden-v6-1';
const ESTATICOS = [
    '/offline.html',
    '/css/eden.css',
    '/js/eden.js',
    '/js/tablero.js',
    '/js/vendor/alpine-csp.min.js',
    '/js/vendor/chart.umd.js',
    '/iconos/icono-192.png',
    '/iconos/icono-512.png'
];

self.addEventListener('install', (evento) => {
    evento.waitUntil(caches.open(VERSION).then((cache) => cache.addAll(ESTATICOS)));
    self.skipWaiting();
});

self.addEventListener('activate', (evento) => {
    evento.waitUntil(
        caches.keys().then((claves) =>
            Promise.all(claves.filter((c) => c !== VERSION).map((c) => caches.delete(c))))
    );
    self.clients.claim();
});

self.addEventListener('fetch', (evento) => {
    const peticion = evento.request;
    if (peticion.method !== 'GET') {
        return; // formularios y acciones van siempre directo al servidor
    }
    const url = new URL(peticion.url);
    if (url.origin !== self.location.origin) {
        return;
    }

    if (peticion.mode === 'navigate') {
        evento.respondWith(fetch(peticion).catch(() => caches.match('/offline.html')));
        return;
    }

    const esEstatico = /^\/(css|js|iconos)\//.test(url.pathname);
    if (esEstatico) {
        // Cache primero y actualizacion en segundo plano.
        evento.respondWith(
            caches.open(VERSION).then((cache) =>
                cache.match(peticion).then((guardado) => {
                    const deRed = fetch(peticion).then((respuesta) => {
                        if (respuesta.ok) {
                            cache.put(peticion, respuesta.clone());
                        }
                        return respuesta;
                    }).catch(() => guardado);
                    return guardado || deRed;
                }))
        );
    }
});
