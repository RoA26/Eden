# Publicar Eden en tu droplet de DigitalOcean

Al terminar tendrás Eden en una dirección como `https://eden-roger.duckdns.org`,
con HTTPS automático, accesible desde cualquier teléfono e instalable como app.

**Arquitectura:** internet → **Caddy** (HTTPS, puertos 80/443) → **Eden** → **PostgreSQL**.
Solo Caddy queda expuesto; la app y la base de datos viven en la red interna de Docker.

En los comandos, cambia `IP_DEL_DROPLET` por la IP que aparece en tu panel de DigitalOcean.

---

## 1. Conseguir un nombre (gratis con DuckDNS)

1. Entra a https://www.duckdns.org e inicia sesión con Google.
2. En *sub domain* escribe un nombre (por ejemplo `eden-roger`) y pulsa **add domain**.
3. En el campo *current ip* de ese dominio, pega la IP de tu droplet y pulsa **update ip**.

Comprueba desde tu PC (debe responder la IP del droplet):

```bash
getent hosts eden-roger.duckdns.org
```

> Si más adelante compras un dominio propio, en su panel creas un registro **A**
> apuntando a la IP del droplet y cambias la línea `DOMINIO` del `.env`. Nada más.

## 2. Subir Eden al droplet (desde tu PC)

```bash
cd ~/Descargas            # donde quedó el zip
scp Eden-v5.zip root@IP_DEL_DROPLET:/opt/
ssh root@IP_DEL_DROPLET
```

Ya dentro del droplet:

```bash
apt-get update && apt-get install -y unzip
cd /opt && unzip Eden-v5.zip && mv Eden-app.V1 eden && cd /opt/eden
```

## 3. Preparar el servidor (una sola vez, unos 5 minutos)

```bash
sudo bash scripts/preparar-servidor.sh
```

Instala Docker, activa el firewall (solo SSH, HTTP y HTTPS), crea 2 GB de memoria
de intercambio (sin ella, 1 GB de RAM no alcanza para compilar), pone la hora de
Colombia y activa las actualizaciones de seguridad automáticas.

## 4. Configurar los secretos

```bash
cp .env.ejemplo .env
openssl rand -hex 24      # ejecútalo 3 veces: un valor distinto para cada clave
nano .env                 # Ctrl+O guarda, Ctrl+X sale
```

Llena `DOMINIO` (sin `https://`), `DB_PASSWORD`, `APP_CODIGO_INVITACION` y `APP_CLAVE_RECORDARME`.

## 5. (Opcional) Traer los datos que ya tienes en tu PC

Hazlo **antes** del primer arranque. En tu PC:

```bash
docker exec eden-appv1-postgres-1 pg_dump -U finanzas --no-owner --no-acl finanzas > eden-datos.sql
scp eden-datos.sql root@IP_DEL_DROPLET:/opt/eden/
```

En el droplet:

```bash
cd /opt/eden
docker compose -f docker-compose.prod.yml up -d postgres
sleep 15
docker compose -f docker-compose.prod.yml exec -T postgres psql -U eden -d eden < eden-datos.sql
rm eden-datos.sql
```

Si alguna cuenta quedó con la clave `Admin123` del rescate, cámbiala en cuanto entres.

## 6. Arrancar Eden

```bash
cd /opt/eden
docker compose -f docker-compose.prod.yml up -d --build
```

La primera vez tarda **entre 5 y 15 minutos**: descarga dependencias y compila con
poca memoria. Las siguientes son más rápidas. Para ver cómo va:

```bash
docker compose -f docker-compose.prod.yml logs -f app     # Ctrl+C para salir
```

Cuando aparezca `Started EdenApplication`, abre `https://TU_DOMINIO` en el navegador.
Caddy obtiene el certificado HTTPS en los primeros segundos.

## 7. Instalar en el teléfono

Abre `https://TU_DOMINIO` en Chrome, inicia sesión (deja activo "Mantener la sesión")
y en el menú de Chrome elige **Instalar app**. En iPhone: Safari → Compartir →
**Agregar a inicio**.

Tu papá hace lo mismo: crea su cuenta con el código de invitación del `.env`.

## 8. Respaldos automáticos (recomendado)

```bash
crontab -e
```

Agrega esta línea (respaldo diario a las 3:00 a. m., se guardan 14 días):

```
0 3 * * * /opt/eden/scripts/respaldo.sh >> /var/log/eden-respaldo.log 2>&1
```

Los respaldos quedan en `/opt/eden/respaldos`. Como están en el mismo servidor,
de vez en cuando bájate una copia a tu PC:

```bash
scp root@IP_DEL_DROPLET:/opt/eden/respaldos/*.sql.gz ~/respaldos-eden/
```

Restaurar un respaldo (sobre una base vacía):

```bash
gunzip -c respaldos/eden-AAAA-MM-DD-HHMM.sql.gz | docker compose -f docker-compose.prod.yml exec -T postgres psql -U eden -d eden
```

---

## Uso diario

**Alguien olvidó su contraseña:** la persona toca "¿Olvidaste tu contraseña?" en el
teléfono. Tú entras al droplet y ves el PIN con:

```bash
bash /opt/eden/scripts/ver-pin.sh
```

**Actualizar a una versión nueva de Eden:** sube el zip nuevo como en el paso 2 y:

```bash
cd /opt && unzip -o Eden-vX.zip && cp -r Eden-app.V1/. eden/ && rm -rf Eden-app.V1
cd /opt/eden && docker compose -f docker-compose.prod.yml up -d --build
```

Tu `.env` y tus datos se conservan; Flyway aplica las migraciones nuevas solo.

**Ver el estado:** `docker compose -f docker-compose.prod.yml ps`

## Si algo falla

| Síntoma | Qué revisar |
|---|---|
| El navegador dice que el sitio no es seguro | DuckDNS debe apuntar a la IP correcta (paso 1). Mira `docker compose -f docker-compose.prod.yml logs caddy` |
| `502 Bad Gateway` | La app todavía está arrancando o falló: `docker compose -f docker-compose.prod.yml logs app` |
| La compilación se corta o se congela | Verifica la memoria de intercambio con `free -h` (debe mostrar 2.0Gi en *Swap*) |
| "Demasiados intentos fallidos" | Protección contra robots: tras 5 claves erradas desde un mismo lugar, espera 15 minutos |

## Seguridad incluida

- HTTPS obligatorio con certificado renovado automáticamente.
- Firewall: solo SSH (22), HTTP (80) y HTTPS (443). PostgreSQL no es accesible desde internet.
- Límite de intentos de inicio de sesión: 5 fallos por usuario y lugar, o 20 por lugar, bloquean 15 minutos.
- Registro solo con código de invitación; contraseñas con BCrypt.
- fail2ban contra ataques al SSH y actualizaciones de seguridad automáticas de Ubuntu.
