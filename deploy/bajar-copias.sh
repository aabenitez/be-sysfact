#!/usr/bin/env bash
# Baja a tu máquina las copias de seguridad del VPS, para que no se pierdan si se pierde el VPS.
# Solo baja las que faltan, verifica que la base se pueda leer y avisa si la última copia es vieja.
# Se ejecuta desde WSL, en la carpeta deploy:  bash bajar-copias.sh
# No pide contraseñas si entrás al VPS con clave SSH (ver la guía, paso 0).
set -euo pipefail

AQUI="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
source "$AQUI/config.env"

DESTINO="${BACKUP_LOCAL:-$HOME/backups-sysfact}"
DIAS_LOCAL="${BACKUP_LOCAL_DIAS:-90}"
mkdir -p "$DESTINO"
chmod 700 "$DESTINO" 2>/dev/null || true   # en carpetas de Windows (/mnt/c) no aplica

echo "==> Bajando copias de $SERVIDOR:$BACKUP_DIR a $DESTINO"
# --ignore-existing: una copia ya bajada no cambia. Sin --delete: lo que el VPS borra a los
# $BACKUP_DIAS días se conserva acá hasta $DIAS_LOCAL días.
rsync -a --chmod=F600 --ignore-existing --itemize-changes -e "ssh -p $SSH_PUERTO" \
  --include='sysfact_*.dump' --include='uploads_*.tar.gz' --exclude='*' \
  "$SERVIDOR:$BACKUP_DIR/" "$DESTINO/" | sed -n 's/^>f[^ ]* /    ✔ /p'

ULTIMA="$(ls -1t "$DESTINO"/sysfact_*.dump 2>/dev/null | head -1 || true)"
if [[ -z "$ULTIMA" ]]; then
  echo "    ✘ No hay ninguna copia de la base. Revisá en el VPS: sudo tail /var/log/sysfact-backup.log" >&2
  exit 1
fi

echo "==> Verificando la última copia: $(basename "$ULTIMA")"
if ! command -v pg_restore > /dev/null; then
  echo "    (pg_restore no está instalado en esta máquina: no se verificó acá; el VPS la verifica al crearla)"
elif ERR="$(pg_restore --list "$ULTIMA" 2>&1 > /dev/null)"; then
  echo "    ✔ Se puede restaurar ($(du -h "$ULTIMA" | cut -f1))"
elif [[ "$ERR" == *"unsupported version"* ]]; then
  # La copia viene de un PostgreSQL más nuevo que el de esta máquina: no es un error de la copia.
  echo "    (tu pg_restore es $(pg_restore --version | grep -o '[0-9]\+' | head -1) y la copia viene de un PostgreSQL más nuevo:"
  echo "     no se puede verificar acá; el VPS la verifica al crearla)"
else
  echo "    ✘ La copia está dañada: $ERR" >&2
  exit 1
fi

EDAD_HORAS=$(( ( $(date +%s) - $(stat -c %Y "$ULTIMA") ) / 3600 ))
if (( EDAD_HORAS > 36 )); then
  echo "    ⚠ La última copia tiene $EDAD_HORAS horas: la copia diaria del VPS no está funcionando." >&2
  echo "      Revisá en el VPS: sudo tail /var/log/sysfact-backup.log" >&2
fi

if (( DIAS_LOCAL > 0 )); then
  find "$DESTINO" -type f \( -name 'sysfact_*.dump' -o -name 'uploads_*.tar.gz' \) -mtime +"$DIAS_LOCAL" -delete
fi
echo
echo "Listo: $(ls -1 "$DESTINO"/sysfact_*.dump | wc -l) copias de la base en $DESTINO"
