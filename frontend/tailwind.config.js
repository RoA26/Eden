/**
 * Sistema de diseno "Cozy Fintech" de Eden.
 * Oscuro primero: fondo verde asfalto, superficies de cristal suave,
 * esmeralda para lo que entra y coral para lo que sale.
 * @type {import('tailwindcss').Config}
 */
module.exports = {
  content: [
    '../src/main/resources/templates/**/*.html',
    '../src/main/resources/static/js/eden.js',
    '../src/main/resources/static/offline.html'
  ],
  theme: {
    extend: {
      colors: {
        fondo: { DEFAULT: '#121614', hondo: '#0C0F0D', panel: '#151A17' },
        superficie: { DEFAULT: 'rgba(255,255,255,0.03)', alta: 'rgba(255,255,255,0.06)' },
        borde: { DEFAULT: 'rgba(255,255,255,0.05)', fuerte: 'rgba(255,255,255,0.10)' },
        tinta: { DEFAULT: '#EEF1EC', suave: '#9DAAA2', tenue: '#6B7A72' },
        esmeralda: { DEFAULT: '#10B981', claro: '#34D399', hondo: '#064E3B' },
        coral: { DEFAULT: '#EF4444', claro: '#F87171' },
        ambar: { DEFAULT: '#F59E0B' }
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif']
      },
      boxShadow: {
        suave: '0 8px 30px -12px rgba(0,0,0,0.55)',
        brillo: '0 10px 30px -10px rgba(16,185,129,0.45)'
      },
      keyframes: {
        'pulso-sube': {
          '0%': { color: '#EEF1EC', textShadow: '0 0 0 rgba(16,185,129,0)' },
          '30%': { color: '#34D399', textShadow: '0 0 28px rgba(16,185,129,0.45)' },
          '100%': { color: '#EEF1EC', textShadow: '0 0 0 rgba(16,185,129,0)' }
        },
        'pulso-baja': {
          '0%': { color: '#EEF1EC', textShadow: '0 0 0 rgba(239,68,68,0)' },
          '30%': { color: '#F87171', textShadow: '0 0 28px rgba(239,68,68,0.40)' },
          '100%': { color: '#EEF1EC', textShadow: '0 0 0 rgba(239,68,68,0)' }
        },
        aparecer: {
          '0%': { opacity: '0', transform: 'translateY(6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        }
      },
      animation: {
        'pulso-sube': 'pulso-sube 1.1s ease-out',
        'pulso-baja': 'pulso-baja 1.1s ease-out',
        aparecer: 'aparecer 0.35s ease-out both'
      }
    }
  },
  plugins: []
};
