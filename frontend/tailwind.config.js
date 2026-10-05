/**
 * Sistema de diseno "Cozy Fintech" de Eden.
 * Oscuro (asfalto #121614) y claro (blanco perla #F8F9FA), cristal suave,
 * esmeralda para lo que entra y coral para lo que sale.
 * @type {import('tailwindcss').Config}
 */
/** Color de tema con soporte de opacidad (bg-fondo/70, text-esmeralda/80...). */
const v = (nombre) => `rgb(var(--c-${nombre}) / <alpha-value>)`;

module.exports = {
  content: [
    '../src/main/resources/templates/**/*.html',
    '../src/main/resources/static/js/eden.js',
    '../src/main/resources/static/offline.html'
  ],
  // El tema se elige con la clase "dark" en <html> (static/js/tema.js y el
  // interruptor Sol/Luna). Los colores son variables CSS definidas en
  // estilos/eden.css para cada tema, asi todas las clases cambian solas.
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        fondo: { DEFAULT: v('fondo'), hondo: v('fondo-hondo'), panel: v('fondo-panel') },
        superficie: {
          DEFAULT: 'rgb(var(--c-superficie) / var(--a-superficie))',
          alta: 'rgb(var(--c-superficie) / var(--a-superficie-alta))'
        },
        borde: {
          DEFAULT: 'rgb(var(--c-realce) / var(--a-borde))',
          fuerte: 'rgb(var(--c-realce) / var(--a-borde-fuerte))'
        },
        // Tinte neutro: blanco sobre oscuro, tinta sobre claro (bg-realce/5 = "un poco mas claro/oscuro")
        realce: v('realce'),
        hundido: 'rgb(var(--c-hundido) / var(--a-hundido))',
        tinta: { DEFAULT: v('tinta'), suave: v('tinta-suave'), tenue: v('tinta-tenue') },
        // Fondo de los botones principales: el esmeralda de marca, igual en ambos temas
        marca: '#10B981',
        esmeralda: { DEFAULT: v('esmeralda'), claro: v('esmeralda-claro'), hondo: '#064E3B' },
        coral: { DEFAULT: v('coral'), claro: v('coral-claro') },
        ambar: { DEFAULT: v('ambar') }
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif']
      },
      boxShadow: {
        suave: '0 8px 30px -12px rgb(var(--c-sombra) / var(--a-sombra))',
        brillo: '0 10px 30px -10px rgba(16,185,129,0.45)'
      },
      keyframes: {
        'pulso-sube': {
          '0%': { color: 'rgb(var(--c-tinta))', textShadow: '0 0 0 rgba(16,185,129,0)' },
          '30%': { color: 'rgb(var(--c-esmeralda-claro))', textShadow: '0 0 28px rgba(16,185,129,0.45)' },
          '100%': { color: 'rgb(var(--c-tinta))', textShadow: '0 0 0 rgba(16,185,129,0)' }
        },
        'pulso-baja': {
          '0%': { color: 'rgb(var(--c-tinta))', textShadow: '0 0 0 rgba(239,68,68,0)' },
          '30%': { color: 'rgb(var(--c-coral-claro))', textShadow: '0 0 28px rgba(239,68,68,0.40)' },
          '100%': { color: 'rgb(var(--c-tinta))', textShadow: '0 0 0 rgba(239,68,68,0)' }
        },
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(4px)' },
          '100%': { opacity: '1', transform: 'none' }
        },
        aparecer: {
          '0%': { opacity: '0', transform: 'translateY(6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        }
      },
      animation: {
        'pulso-sube': 'pulso-sube 1.1s ease-out',
        'pulso-baja': 'pulso-baja 1.1s ease-out',
        aparecer: 'aparecer 0.35s ease-out both',
        'fade-in': 'fade-in 0.35s ease-out'
      }
    }
  },
  plugins: []
};
