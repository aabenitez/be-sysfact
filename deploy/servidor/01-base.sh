#!/usr/bin/env bash
# Paso 1: actualiza el sistema, zona horaria, firewall, swap y actualizaciones automáticas.
# Uso: sudo bash 01-base.sh
source "$(dirname "$0")/comun.sh"

paso "Actualizando paquetes"
apt-get update -y
DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
DEBIAN_FRONTEND=noninteractive apt-get install -y \
  curl unzip ufw fail2ban unattended-upgrades ca-certificates
ok "Sistema actualizado"

paso "Zona horaria: $ZONA_HORARIA"
timedatectl set-timezone "$ZONA_HORARIA"
ok "$(timedatectl show -p Timezone --value)"

paso "Firewall: solo SSH ($SSH_PUERTO), HTTP y HTTPS"
ufw default deny incoming
ufw default allow outgoing
ufw allow "$SSH_PUERTO"/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
ok "Firewall activo. WildFly (8080, 9990) y PostgreSQL (5432) quedan cerrados desde internet."

paso "Swap de 1 GB (seguro ante picos de memoria)"
if swapon --show | grep -q .; then
  ok "Ya hay swap configurado: $(swapon --show --noheadings | awk '{print $3}' | head -1)"
else
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
  sysctl -w vm.swappiness=10 >/dev/null
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-sysfact-swap.conf
  ok "Swap creado"
fi

paso "Actualizaciones de seguridad automáticas y fail2ban (bloquea intentos de SSH)"
dpkg-reconfigure -f noninteractive unattended-upgrades
systemctl enable --now fail2ban
ok "Listo"

echo
echo "Paso 1 terminado. Siguiente: sudo bash 02-postgres.sh"
