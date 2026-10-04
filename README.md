# Eden

Gestión financiera personal: fuentes de ingreso con sus costos, gastos,
cajitas de ahorro, presupuestos y metas. Spring Boot 3 + Thymeleaf +
PostgreSQL, arquitectura en capas.

## Estructura

```
com.eden
├── config        Seguridad y propiedades de la aplicación
├── seguridad     Integración con Spring Security (usuario en sesión)
├── modelo        Entidades del dominio
├── dto           Formularios validados con @Valid
├── persistencia  Repositorios (interfaces + implementación JdbcClient)
├── servicio      Reglas de negocio y transacciones (@Transactional)
├── web           Controladores livianos y manejo global de errores
└── excepcion     Excepciones de dominio
```

## Interfaz ("Cozy Fintech", V6)

Oscura y móvil primero, sin barra inferior. En el teléfono la hamburguesa
abre un menú lateral sobre un fondo oscurecido y desenfocado; desde `md` el
contenido es fluido y desde `lg` el menú es una barra lateral fija y el tablero
se organiza en una rejilla de panel de control.

- **Landing pública** (`/`, plantilla `bienvenida.html`): presentación comercial;
  quien ya tiene sesión va directo a `/inicio`.
- **Producido del día**: modal-calculadora (teclado numérico grande) para la
  fuente principal activa; no exige crear una fuente antes.

- **HTML**: Thymeleaf. Fragmentos reutilizables en `templates/fragmentos/`:
  `base` (`<head>`), `navegacion` (cabecera y menú lateral), `componentes`
  (fila de movimiento, tarjeta de cajita, botón rápido, modal, avisos) y
  `modales` (formularios del tablero).
- **Estilos**: Tailwind CSS **compilado** (no el CDN, que exigiría relajar la
  CSP). Fuente en `frontend/estilos/eden.css` y `frontend/tailwind.config.js`;
  el resultado `static/css/eden.css` se versiona, así que Maven y Docker no
  necesitan Node.
- **Reactividad**: Alpine.js en su build CSP (`static/js/vendor/alpine-csp.min.js`),
  porque la política `script-src 'self'` no permite `eval`. Los componentes
  están en `static/js/eden.js` y las plantillas solo nombran estados y métodos.
- **Botones rápidos** (fricción cero): plantillas como «Moto $5.000» que
  registran el movimiento con un toque (`POST /api/atajos/{id}/usar`), muestran
  un spinner en el botón, animan el saldo total sin recargar y ofrecen "Deshacer".
- **Mejora progresiva**: cada formulario conserva su `th:action` normal; con
  JavaScript, Alpine lo intercepta y lo envía por fetch a `/api/...` (JSON, mismo
  token CSRF). Sin JavaScript todo sigue funcionando con recargas.

Después de cambiar plantillas o estilos, regenerar el CSS (requiere Node 18+):

```
cd frontend
npm install        # la primera vez
npm run css        # o npm run css:vigilar mientras se edita
```

`npm run vendor` vuelve a copiar Alpine desde `node_modules` al actualizarlo.

El esquema de base de datos vive en `src/main/resources/db/migration`
y lo aplica Flyway al arrancar. Una migración aplicada nunca se edita:
los cambios van en un archivo nuevo (`V2__...sql`).

## Módulos (V2)

- **Tablero** (`/inicio`): saldo total, botones rápidos, disponible por repartir, resumen del mes, estado de resultados por fuente, cajitas y gráficas (Chart.js servido localmente en `static/js/vendor`).
- **Registro rápido** (`/registrar`): producido del día por fuente, con su costo opcional. Un solo producido por fuente y día (RN-01).
- **Movimientos** (`/movimientos`): ingresos y gastos con filtros por fecha, tipo, categoría y fuente. Un gasto puede pagarse desde una cajita, lo que genera un retiro asociado (RN-06).
- **Calendario** (`/calendario`): días con producido, montos del día y días sin registro de las fuentes diarias.
- **Cajitas** (`/cajitas`): saldos calculados desde los movimientos, aportes, retiros, rendimientos, saldo inicial y reparto sugerido por porcentajes con cajita "resto" (RN-03 a RN-05).
- **Fuentes y categorías** (`/fuentes`, `/categorias`): se desactivan en lugar de borrarse, para conservar el histórico.

## Seguridad (V5)

- Límite de intentos de inicio de sesión (`seguridad.LimitadorIntentosLogin`): 5 fallos por IP + usuario o 20 por IP bloquean 15 minutos; el filtro corta antes de verificar la contraseña.
- Recuperación de contraseña con PIN temporal impreso en la consola del servidor (`/recuperar`).
- En producción, Caddy termina el HTTPS y Tomcat toma la IP real de `X-Forwarded-For` solo si viene de la red interna de Docker.

## Módulos (V3)

- **Recurrentes** (`/recurrentes`): pagos o ingresos que se repiten (semanal, cada 15 días, mensual, anual). Aparecen en el inicio unos días antes y se confirman con el monto real u omiten; el registro pasa por `MovimientoService`, con sus mismas reglas.
- **Presupuestos** (`/presupuestos`): tope mensual por categoría de gasto o costo, con alerta en el inicio desde el 80%.
- **Metas** (`/metas`): vinculadas a una cajita; calculan progreso y aporte mensual sugerido (`modelo.ProgresoMeta`).
- **Compartir** (`/compartir`): acceso de solo lectura concedido y revocable por el titular. Las vistas compartidas viven en `/compartido/{idTitular}/…`, solo exponen GET y verifican el permiso en cada petición.
- **PWA**: `manifest.json`, `sw.js` (solo guarda en caché archivos estáticos, nunca páginas con datos) y "mantener la sesión" por 30 días.

Los montos se escriben en formato colombiano (`85000`, `85.000` o `85.000,50`) y se convierten en `Dinero.parsear`.
La lógica de negocio vive en `servicio`; el cálculo del reparto está aislado en `modelo.CalculadoraReparto`.

## Ejecutar en local

Requisitos: Java 17+, Maven y Docker (con el servicio habilitado).

1. La primera vez, en la carpeta del proyecto: `docker compose up -d`.
   El contenedor de PostgreSQL queda configurado para iniciar solo cada
   vez que arranca Docker, así que este paso no se repite.
2. En VSCodium: panel "Run and Debug" y ejecutar **Eden (dev)**
   (requiere la extensión "Extension Pack for Java").
   Alternativa en terminal: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
3. Abrir http://localhost:8080 y crear la cuenta en "Crear cuenta" con el
   código de invitación `desarrollo` (solo en el perfil dev).

Para abrirla desde el teléfono en la misma red Wi-Fi, usar la IP local
del PC (`ip -4 addr`), por ejemplo `http://192.168.1.10:8080`.

## Variables de entorno (producción)

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `APP_CODIGO_INVITACION` | Código requerido para crear cuentas (obligatorio) |
| `APP_CLAVE_RECORDARME` | Clave que firma la cookie "mantener la sesión" (obligatoria) |
| `APP_NOMBRE` | Nombre visible de la aplicación |

Guía completa para publicar en un droplet de DigitalOcean con HTTPS: [DESPLIEGUE.md](DESPLIEGUE.md).

## Pruebas

```bash
mvn test
```
