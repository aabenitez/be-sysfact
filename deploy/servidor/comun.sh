#!/usr/bin/env bash
# Funciones comunes de los scripts del servidor. Se carga con: source "$(dirname "$0")/comun.sh"
set -euo pipefail

AQUI="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG="$AQUI/../config.env"

if [[ ! -f "$CONFIG" ]]; then
  echo "Falta $CONFIG. Copiá config.env.example como config.env y completalo." >&2
  exit 1
fi
# shellcheck source=/dev/null
source "$CONFIG"

if [[ $EUID -ne 0 ]]; then
  echo "Este script se ejecuta con sudo: sudo bash $0" >&2
  exit 1
fi

# Archivo con la contraseña de la base que genera 02-postgres.sh (solo lo lee root).
DB_PASS_FILE="/root/.sysfact-db.pass"

paso() { echo; echo "==> $*"; }
ok()   { echo "    ✔ $*"; }
