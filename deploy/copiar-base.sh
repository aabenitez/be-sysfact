#!/usr/bin/env bash
# Copia la base sysfact de tu máquina (WSL) al VPS. Úsalo una sola vez, al instalar.
# ¡Reemplaza todo lo que haya en la base del VPS!
# Se ejecuta desde WSL, en la carpeta deploy:  bash copiar-base.sh
# Pide tu contraseña de sudo local (para leer la base) y la del VPS.
set -euo pipefail

AQUI="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
source "$AQUI/config.env"

SSH=(ssh -p "$SSH_PUERTO")
SCP=(scp -P "$SSH_PUERTO")
DUMP="$(mktemp --suffix=.dump)"
trap 'rm -f "$DUMP"' EXIT

echo "Esto REEMPLAZA la base '$DB_NAME' del VPS ($SERVIDOR) con la de tu máquina."
read -r -p "¿Seguir? Escribí SI para confirmar: " RESP
[[ "$RESP" == "SI" ]] || { echo "Cancelado."; exit 1; }

echo "==> Exportando la base local"
sudo -u postgres pg_dump -Fc --no-owner --no-acl sysfact > "$DUMP"
echo "    ✔ $(du -h "$DUMP" | cut -f1)"

echo "==> Subiendo al VPS"
"${SCP[@]}" "$DUMP" "$SERVIDOR:/tmp/sysfact.dump"

echo "==> Restaurando en el VPS"
"${SSH[@]}" "$SERVIDOR" "sudo bash -s" <<EOF
set -euo pipefail
systemctl stop wildfly || true
sudo -u postgres dropdb --if-exists "$DB_NAME"
sudo -u postgres createdb -O "$DB_USER" -E UTF8 "$DB_NAME"
# Las tablas quedan a nombre del usuario de la aplicación.
sudo -u postgres pg_restore --no-owner --role="$DB_USER" -d "$DB_NAME" < /tmp/sysfact.dump
rm -f /tmp/sysfact.dump
systemctl start wildfly
echo "    ✔ Base restaurada"
EOF

echo
echo "Siguiente: en el VPS, sudo bash deploy/servidor/06-parametros.sh"
