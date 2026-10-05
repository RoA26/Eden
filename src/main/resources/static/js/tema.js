// Tema claro/oscuro. Se carga en <head> SIN defer para poner la clase "dark"
// antes de la primera pintura (evita el destello del tema equivocado). Es un
// archivo externo porque la CSP (script-src 'self') no permite scripts en linea.
// Preferencia guardada en localStorage ("eden:tema" = "oscuro" | "claro");
// sin preferencia, sigue al sistema y, si no lo indica, usa el oscuro.
(function () {
    var guardado = null;
    try {
        guardado = window.localStorage.getItem('eden:tema');
    } catch (e) {
        // almacenamiento bloqueado (modo privado): se usa la preferencia del sistema
    }
    var sistemaClaro = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches;
    var oscuro = guardado ? guardado === 'oscuro' : !sistemaClaro;
    document.documentElement.classList.toggle('dark', oscuro);
})();
