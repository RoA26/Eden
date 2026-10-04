// Eden - interacciones de la interfaz con Alpine.js (build CSP).
//
// La politica de seguridad (script-src 'self') no permite eval, asi que se usa
// el build CSP de Alpine: toda la logica vive aqui, registrada con
// Alpine.data / Alpine.store, y las plantillas solo nombran estados y metodos.
//
// Mejora progresiva: cada formulario tiene su th:action normal y funciona sin
// JavaScript. Con JavaScript, Alpine lo intercepta y lo envia por fetch al
// endpoint de data-api (JSON), sin recargar la pagina.
(function () {
    'use strict';

    // ------------------------------------------------------------------ utilidades

    const FORMATO_PESOS = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
    const SIN_MOVIMIENTO = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /** Mismo formato que Dinero.formatear en el servidor: "$ 85.000" / "-$ 2.000". */
    function pesos(valor) {
        const n = Math.round(Number(valor) || 0);
        return (n < 0 ? '-$ ' : '$ ') + FORMATO_PESOS.format(Math.abs(n));
    }

    function meta(nombre) {
        const nodo = document.querySelector('meta[name="' + nombre + '"]');
        return nodo ? nodo.getAttribute('content') : null;
    }

    /** Ruta de la aplicacion respetando el context path (meta eden:base = @{/}). */
    function ruta(relativa) {
        return (meta('eden:base') || '/') + relativa.replace(/^\//, '');
    }

    function cabecerasCsrf() {
        const token = meta('_csrf');
        const cabecera = meta('_csrf_header');
        const cabeceras = { Accept: 'application/json' };
        if (token && cabecera) {
            cabeceras[cabecera] = token;
        }
        return cabeceras;
    }

    /**
     * POST que espera JSON. Si la sesion vencio, Spring Security responde con
     * la pagina de login (HTML): en ese caso se recarga para que el usuario
     * vuelva a entrar.
     */
    async function publicar(url, cuerpo) {
        const respuesta = await fetch(url, {
            method: 'POST',
            body: cuerpo || new FormData(),
            headers: cabecerasCsrf(),
            credentials: 'same-origin'
        });
        const tipo = respuesta.headers.get('content-type') || '';
        if (tipo.indexOf('application/json') === -1) {
            window.location.reload();
            const sesion = new Error('Tu sesión venció.');
            sesion.sesion = true;
            throw sesion;
        }
        const datos = await respuesta.json();
        if (!respuesta.ok) {
            const error = new Error(datos.error || 'No se pudo completar la acción.');
            error.datos = datos;
            throw error;
        }
        return datos;
    }

    function mensajeDeError(error) {
        if (error && error.datos) {
            return error.message;
        }
        return 'No hay conexión con Eden. Revisa tu internet e intenta de nuevo.';
    }

    function vibrar() {
        if (navigator.vibrate) {
            navigator.vibrate(12);
        }
    }

    // ------------------------------------------------------------------ saldo animado

    /** Cuenta del valor anterior al nuevo y hace "latir" la cifra en verde o coral. */
    function animarCifra(el, hasta, textoFinal) {
        const desde = Number(el.dataset.valor || 0);
        el.dataset.valor = String(hasta);

        if (el.hasAttribute('data-colorear')) {
            el.classList.toggle('monto-ingreso', hasta >= 0);
            el.classList.toggle('monto-gasto', hasta < 0);
        }
        if (desde !== hasta && el.hasAttribute('data-pulso')) {
            el.classList.remove('pulso-sube', 'pulso-baja');
            void el.offsetWidth; // reinicia la animacion CSS
            el.classList.add(hasta > desde ? 'pulso-sube' : 'pulso-baja');
        }
        if (SIN_MOVIMIENTO || desde === hasta) {
            el.textContent = textoFinal;
            return;
        }
        const inicio = performance.now();
        const duracion = 750;
        function paso(ahora) {
            const avance = Math.min(1, (ahora - inicio) / duracion);
            const suavizado = 1 - Math.pow(1 - avance, 3);
            el.textContent = pesos(desde + (hasta - desde) * suavizado);
            if (avance < 1) {
                requestAnimationFrame(paso);
            } else {
                el.textContent = textoFinal;
            }
        }
        requestAnimationFrame(paso);
    }

    /** saldo = { total: {valor, texto}, disponible: {...}, ... } tal como lo devuelve ApiController. */
    function actualizarSaldo(saldo) {
        if (!saldo) {
            return;
        }
        Object.keys(saldo).forEach(function (clave) {
            document.querySelectorAll('[data-cifra="' + clave + '"]').forEach(function (el) {
                animarCifra(el, Number(saldo[clave].valor), saldo[clave].texto);
            });
        });
    }

    // ------------------------------------------------------------------ secciones parciales

    /**
     * Vuelve a pedir secciones del tablero (fragmentos Thymeleaf) y las
     * reemplaza en el DOM. Alpine inicializa solo lo nuevo.
     */
    async function refrescar(secciones) {
        for (const nombre of secciones) {
            const actual = document.getElementById('seccion-' + nombre);
            if (!actual || !actual.dataset.parcial) {
                continue;
            }
            try {
                const respuesta = await fetch(actual.dataset.parcial, {
                    headers: { Accept: 'text/html' },
                    credentials: 'same-origin'
                });
                if (!respuesta.ok || respuesta.redirected) {
                    continue;
                }
                const plantilla = document.createElement('template');
                plantilla.innerHTML = (await respuesta.text()).trim();
                const nuevo = plantilla.content.firstElementChild;
                if (nuevo && nuevo.id === actual.id) {
                    nuevo.classList.add('animate-aparecer');
                    actual.replaceWith(nuevo);
                }
            } catch (e) {
                // Sin conexion: la seccion queda como estaba hasta la proxima carga.
            }
        }
    }

    function refrescarDespues(secciones, milisegundos) {
        window.setTimeout(function () { refrescar(secciones); }, milisegundos);
    }

    // ------------------------------------------------------------------ categorias nuevas

    /** Agrega la categoria recien creada a todos los selectores que admiten su tipo. */
    function agregarCategoria(categoria) {
        document.querySelectorAll('[data-opciones-categoria="' + categoria.tipo + '"]').forEach(function (contenedor) {
            const opcion = document.createElement('option');
            opcion.value = categoria.id;
            opcion.textContent = categoria.nombre;
            contenedor.appendChild(opcion);
            const selector = contenedor.tagName === 'SELECT' ? contenedor : contenedor.closest('select');
            if (selector && selector.closest('[data-modal="' + (Alpine.store('ui').volverA || '') + '"]')) {
                selector.value = String(categoria.id);
            }
        });
    }

    // ------------------------------------------------------------------ componentes

    document.addEventListener('alpine:init', function () {
        const Alpine = window.Alpine;

        // Estado global de la interfaz: menu lateral y modal abierto.
        Alpine.store('ui', {
            menu: false,
            modal: null,
            volverA: null,

            abrirMenu() {
                this.modal = null;
                this.menu = true;
            },
            cerrarMenu() {
                this.menu = false;
            },
            /** volverA: modal al que se regresa al terminar (ej. crear categoria desde "movimiento"). */
            abrirModal(nombre, volverA) {
                this.menu = false;
                this.volverA = volverA || null;
                this.modal = nombre;
                window.setTimeout(function () {
                    const panel = document.querySelector('[data-modal="' + nombre + '"]');
                    if (panel && (panel.hasAttribute('data-sin-autofoco') || panel.querySelector('[data-sin-autofoco]'))) {
                        return; // ej. la calculadora: en el telefono no debe abrir el teclado del sistema
                    }
                    const campo = panel && panel.querySelector('[autofocus], input:not([type=hidden]):not([type=radio]), select');
                    if (campo) {
                        campo.focus({ preventScroll: true });
                    }
                }, 120);
            },
            /** Para enlaces: abre el modal si esta pagina lo tiene; si no, deja que el enlace navegue. */
            abrirSiExiste(nombre, evento) {
                if (document.querySelector('[data-modal="' + nombre + '"]')) {
                    evento.preventDefault();
                    this.abrirModal(nombre);
                }
            },
            cerrarModal() {
                const volver = this.volverA;
                this.volverA = null;
                this.modal = volver;
            },
            cerrarTodo() {
                this.menu = false;
                this.modal = null;
                this.volverA = null;
            },
            get bloqueado() {
                return this.menu || this.modal !== null;
            }
        });

        // Avisos flotantes ("tostadas"), con accion opcional como "Deshacer".
        Alpine.store('avisos', {
            lista: [],
            siguiente: 1,

            mostrar(texto, tipo, accion) {
                const id = this.siguiente++;
                this.lista.push({ id: id, texto: texto, tipo: tipo || 'exito', accion: accion || null });
                if (this.lista.length > 3) {
                    this.lista.shift();
                }
                const self = this;
                window.setTimeout(function () { self.cerrar(id); }, accion ? 6500 : 3800);
            },
            cerrar(id) {
                this.lista = this.lista.filter(function (aviso) { return aviso.id !== id; });
            },
            ejecutar(aviso) {
                this.cerrar(aviso.id);
                if (aviso.accion && aviso.accion.ejecutar) {
                    aviso.accion.ejecutar();
                }
            }
        });

        // Bloquea el scroll del fondo con el menu o un modal abiertos.
        Alpine.effect(function () {
            document.documentElement.classList.toggle('overflow-hidden', Alpine.store('ui').bloqueado);
        });

        // Raiz de cada pagina (<body>): muestra como tostada los avisos flash del servidor.
        Alpine.data('aplicacion', function () {
            return {
                init() {
                    document.querySelectorAll('[data-aviso-inicial]').forEach(function (nodo) {
                        Alpine.store('avisos').mostrar(nodo.textContent.trim(), nodo.getAttribute('data-aviso-inicial'));
                        nodo.remove();
                    });
                },
                escape() {
                    Alpine.store('ui').cerrarTodo();
                }
            };
        });

        // Formularios de los modales: envio por fetch y errores por campo.
        function formularioAjax(extra) {
            return Object.assign({
                enviando: false,
                errores: {},
                errorGeneral: '',

                async enviar(evento) {
                    const formulario = evento.target;
                    if (this.enviando) {
                        return;
                    }
                    this.enviando = true;
                    this.errores = {};
                    this.errorGeneral = '';
                    try {
                        const datos = await publicar(formulario.dataset.api, new FormData(formulario));
                        this.exito(datos, formulario);
                    } catch (error) {
                        if (error.sesion) {
                            return;
                        }
                        const errores = (error.datos && error.datos.errores) || {};
                        const sinCampoVisible = Object.keys(errores).some(function (campo) {
                            return !formulario.querySelector('[data-error-de="' + campo + '"]');
                        });
                        this.errores = errores;
                        if (!Object.keys(errores).length || sinCampoVisible) {
                            this.errorGeneral = mensajeDeError(error);
                        }
                    } finally {
                        this.enviando = false;
                    }
                },

                exito(datos, formulario) {
                    vibrar();
                    actualizarSaldo(datos.saldo);
                    if (datos.categoria) {
                        agregarCategoria(datos.categoria);
                    }
                    Alpine.store('avisos').mostrar(datos.mensaje, 'exito');
                    formulario.reset();
                    this.alReiniciar();
                    const secciones = (formulario.dataset.refrescar || '').split(',').filter(Boolean);
                    refrescar(secciones.filter(function (s) { return s !== 'resumenMes'; }));
                    if (secciones.indexOf('resumenMes') !== -1) {
                        refrescarDespues(['resumenMes'], 900); // despues de que termine la animacion
                    }
                    Alpine.store('ui').cerrarModal();
                    if (formulario.dataset.irA) {
                        window.setTimeout(function () { window.location.assign(formulario.dataset.irA); }, 900);
                    }
                },

                alReiniciar() {},

                tieneError(campo) {
                    return Boolean(this.errores[campo]);
                },
                claseError(campo) {
                    return this.errores[campo] ? 'con-error' : '';
                }
            }, extra || {});
        }

        Alpine.data('formularioAjax', function () {
            return formularioAjax();
        });

        // Formularios con selector Gasto / Ingreso (movimiento y boton rapido).
        Alpine.data('formularioConTipo', function () {
            return formularioAjax({
                tipo: 'GASTO',
                init() {
                    const marcado = this.$el.querySelector('input[name="tipo"]:checked');
                    this.tipo = marcado ? marcado.value : 'GASTO';
                },
                elegirGasto() {
                    this.tipo = 'GASTO';
                },
                elegirIngreso() {
                    this.tipo = 'INGRESO';
                },
                // Metodos y no getters: Object.assign copiaria el valor del getter, no el getter.
                esGasto() {
                    return this.tipo === 'GASTO';
                },
                esIngreso() {
                    return this.tipo === 'INGRESO';
                },
                alReiniciar() {
                    this.tipo = 'GASTO';
                }
            });
        });

        // Calculadora del producido del dia: teclado numerico grande, sin formularios largos.
        Alpine.data('calculadora', function () {
            return formularioAjax({
                monto: '',

                pulsar(digitos) {
                    const nuevo = (this.monto + digitos).replace(/^0+/, '');
                    if (nuevo.length <= 12) {
                        this.monto = nuevo;
                    }
                },
                borrar() {
                    this.monto = this.monto.slice(0, -1);
                },
                sumar(cantidad) {
                    this.monto = String((Number(this.monto) || 0) + cantidad);
                },
                /** Escritura directa con el teclado fisico: se conservan solo los digitos. */
                escribir(evento) {
                    this.monto = evento.target.value.replace(/\D/g, '').replace(/^0+/, '').slice(0, 12);
                    evento.target.value = this.texto();
                },
                texto() {
                    return this.monto ? pesos(this.monto) : '';
                },
                vacio() {
                    return !this.monto;
                },
                alReiniciar() {
                    this.monto = '';
                }
            });
        });

        // Menu del encabezado publico (landing) en el telefono.
        Alpine.data('menuPublico', function () {
            return {
                abierto: false,
                alternar() {
                    this.abierto = !this.abierto;
                },
                cerrar() {
                    this.abierto = false;
                }
            };
        });

        // Boton rapido: un toque registra el movimiento y actualiza el saldo.
        Alpine.data('botonRapido', function () {
            return {
                cargando: false,

                async usar(evento) {
                    const formulario = evento.target;
                    if (this.cargando) {
                        return;
                    }
                    this.cargando = true;
                    try {
                        const datos = await publicar(formulario.dataset.api, new FormData(formulario));
                        vibrar();
                        actualizarSaldo(datos.saldo);
                        Alpine.store('avisos').mostrar(datos.mensaje, 'exito', datos.idMovimiento ? {
                            etiqueta: 'Deshacer',
                            ejecutar: function () { deshacer(datos.idMovimiento); }
                        } : null);
                        refrescar(['ultimos']);
                        refrescarDespues(['resumenMes'], 900);
                    } catch (error) {
                        if (!error.sesion) {
                            Alpine.store('avisos').mostrar(mensajeDeError(error), 'error');
                        }
                    } finally {
                        this.cargando = false;
                    }
                }
            };
        });

        async function deshacer(idMovimiento) {
            try {
                const datos = await publicar(ruta('api/movimientos/' + idMovimiento + '/eliminar'));
                actualizarSaldo(datos.saldo);
                Alpine.store('avisos').mostrar(datos.mensaje, 'exito');
                refrescar(['ultimos']);
                refrescarDespues(['resumenMes'], 900);
            } catch (error) {
                if (!error.sesion) {
                    Alpine.store('avisos').mostrar(mensajeDeError(error), 'error');
                }
            }
        }

        // Cuadricula de botones rapidos: modo edicion para quitarlos.
        Alpine.data('seccionAtajos', function () {
            return {
                editando: false,

                alternarEdicion() {
                    this.editando = !this.editando;
                },
                async quitar(evento) {
                    const formulario = evento.target;
                    if (!window.confirm(formulario.getAttribute('data-pregunta') || '¿Eliminar este botón?')) {
                        return;
                    }
                    try {
                        const datos = await publicar(formulario.dataset.api, new FormData(formulario));
                        Alpine.store('avisos').mostrar(datos.mensaje, 'exito');
                        refrescar(['atajos']);
                    } catch (error) {
                        if (!error.sesion) {
                            Alpine.store('avisos').mostrar(mensajeDeError(error), 'error');
                        }
                    }
                }
            };
        });

        // Pestanas de las graficas del tablero.
        Alpine.data('pestanasGraficas', function () {
            return {
                activa: 'producido',

                elegir(nombre) {
                    this.activa = nombre;
                    // Chart.js recalcula el tamano cuando el lienzo vuelve a ser visible.
                    window.requestAnimationFrame(function () { window.dispatchEvent(new Event('resize')); });
                },
                clase(nombre) {
                    return this.activa === nombre ? 'segmento-activo' : '';
                }
            };
        });
    });

    // ------------------------------------------------------------------ utilidades sin Alpine

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
            navigator.serviceWorker.register(ruta('sw.js')).catch(function (error) {
                console.warn('No se pudo registrar el service worker', error);
            });
        });
    }
})();
