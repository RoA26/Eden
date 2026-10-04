// Copia las librerias de terceros a static/js/vendor para servirlas desde el
// propio dominio: la politica CSP (script-src 'self') no permite CDNs.
import { copyFileSync } from 'node:fs';

const destino = '../src/main/resources/static/js/vendor/';
copyFileSync('node_modules/@alpinejs/csp/dist/cdn.min.js', destino + 'alpine-csp.min.js');
console.log('Alpine.js (build CSP) copiado a ' + destino);
