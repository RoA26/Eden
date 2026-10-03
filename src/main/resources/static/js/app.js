// Confirmacion antes de enviar formularios destructivos: <form data-confirmar="¿Seguro?">
document.addEventListener('submit', function (evento) {
    const formulario = evento.target;
    const mensaje = formulario.getAttribute && formulario.getAttribute('data-confirmar');
    if (mensaje && !window.confirm(mensaje)) {
        evento.preventDefault();
    }
});

// Mostrar u ocultar la contrasena: <button data-alternar-contrasena="idDelInput">
document.addEventListener('click', function (evento) {
    const boton = evento.target.closest ? evento.target.closest('[data-alternar-contrasena]') : null;
    if (!boton) {
        return;
    }
    const campo = document.getElementById(boton.getAttribute('data-alternar-contrasena'));
    if (!campo) {
        return;
    }
    const mostrar = campo.type === 'password';
    campo.type = mostrar ? 'text' : 'password';
    boton.textContent = mostrar ? 'Ocultar' : 'Ver';
    boton.setAttribute('aria-pressed', String(mostrar));
    boton.setAttribute('aria-label', mostrar ? 'Ocultar contraseña' : 'Mostrar contraseña');
    campo.focus({ preventScroll: true });
});

// Registro del service worker (necesario para instalar Eden como app en el telefono).
if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () {
        navigator.serviceWorker.register('/sw.js').catch(function (error) {
            console.warn('No se pudo registrar el service worker', error);
        });
    });
}
