#!/usr/bin/env bash
# Prepara un droplet nuevo de Ubuntu 24.04 para Eden. Se ejecuta UNA vez:
#   sudo bash scripts/preparar-servidor.sh
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
    echo "Ejecuta este script con sudo." >&2
    exit 1
fi
export DEBIAN_FRONTEND=noninteractive

echo "==> Actualizando el sistema"
apt-get update -q
apt-get upgrade -y -q

echo "==> Instalando Docker, firewall y utilidades"
apt-get install -y -q docker.io docker-compose-v2 ufw unattended-upgrades unzip
systemctl enable --now docker

echo "==> Zona horaria de Colombia"
timedatectl set-timezone America/Bogota

echo "==> Memoria de intercambio (2 GB): necesaria para compilar Eden con 1 GB de RAM"
if ! swapon --show | grep -q '/swapfile'; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
echo 'vm.swappiness=10' > /etc/sysctl.d/99-eden.conf
sysctl -p /etc/sysctl.d/99-eden.conf

echo "==> Firewall: solo SSH, HTTP y HTTPS"
ufw default deny incoming
ufw default allow outgoing
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 443/udp
ufw --force enable

echo "==> Actualizaciones de seguridad automaticas"
dpkg-reconfigure -f noninteractive unattended-upgrades

echo "==> Proteccion contra intentos de entrada por SSH (fail2ban)"
if apt-get install -y -q fail2ban && systemctl enable --now fail2ban; then
    echo "fail2ban activo."
else
    echo "AVISO: no se pudo activar fail2ban; el resto quedo configurado." >&2
fi

echo
echo "Servidor listo. Sigue con el paso 5 de DESPLIEGUE.md (archivo .env)."
