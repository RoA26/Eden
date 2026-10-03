# Desplegar Eden y usarlo desde el teléfono

El plan: Eden corre en tu PC (Arch Linux) con Docker y se publica con HTTPS
**solo para tus dispositivos** usando Tailscale. No se abre ningún puerto a
internet y no necesitas dominio ni pagar servidor. El HTTPS es obligatorio
para poder instalar Eden como app en el teléfono.

Limitación: el PC debe estar encendido para que la app responda. Más adelante
se puede mover a un VPS con los mismos archivos.

## 1. Preparar los secretos

```bash
cd ~/dev/EdeN/Eden-app.V1
cp .env.ejemplo .env
openssl rand -hex 24   # ejecútalo tres veces y pega un valor distinto en cada variable de .env
```

## 2. (Opcional) Traer tus datos de desarrollo

Si ya registraste movimientos reales en la base de desarrollo, pásalos a la de
producción **antes** de arrancar la app por primera vez:

```bash
docker compose -f docker-compose.prod.yml up -d postgres
# espera unos segundos a que quede "healthy"
docker exec eden-appv1-postgres-1 pg_dump -U finanzas --no-owner --no-acl finanzas \
  | docker compose -f docker-compose.prod.yml exec -T postgres psql -U eden -d eden
```

## 3. Construir y arrancar

Detén primero la app de desarrollo en VSCodium (ambas usan el puerto 8080).

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml logs -f app   # Ctrl+C para salir
```

Comprueba en el PC: http://127.0.0.1:8080

## 4. Publicar con HTTPS usando Tailscale

```bash
sudo pacman -S tailscale
sudo systemctl enable --now tailscaled
sudo tailscale up            # abre el enlace e inicia sesión (Google o GitHub)
```

En https://login.tailscale.com/admin/dns activa **MagicDNS** y **HTTPS Certificates**. Luego:

```bash
sudo tailscale serve --bg 8080
tailscale serve status       # muestra tu dirección: https://<tu-pc>.<tu-red>.ts.net
```

## 5. Instalar en el teléfono

1. Instala la app **Tailscale** en el teléfono e inicia sesión con la misma cuenta.
2. Abre la dirección `https://<tu-pc>.<tu-red>.ts.net` en Chrome.
3. Inicia sesión en Eden (deja marcado "Mantener la sesión").
4. Menú de Chrome → **Instalar app** (o "Agregar a pantalla principal").

## 6. Tu papá

1. Él crea su cuenta en Eden con el código de invitación de tu `.env`.
2. Para que llegue a tu PC: en https://login.tailscale.com/admin/machines abre
   tu PC → **Share** y compártelo con su correo. Él instala Tailscale con su
   propia cuenta y acepta la invitación (solo verá ese equipo, nada más).
3. Si quieres que él vea tus números: en Eden → Más → **Compartir mi cuenta**,
   escribe su nombre de usuario. Es solo lectura y lo puedes revocar.

## Respaldos

```bash
mkdir -p respaldos
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U eden eden > respaldos/eden-$(date +%F).sql
```

Restaurar (sobre una base vacía):

```bash
docker compose -f docker-compose.prod.yml exec -T postgres psql -U eden -d eden < respaldos/eden-AAAA-MM-DD.sql
```

## Actualizar a una nueva versión

Reemplaza el código, y luego:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Flyway aplica las migraciones nuevas al arrancar; los datos se conservan en el volumen `eden_datos_eden`.
