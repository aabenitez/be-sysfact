#!/usr/bin/env bash
# Paso 2: instala PostgreSQL 16, crea la base y un usuario propio para Sysfact y ajusta la memoria.
# La contraseña del usuario se genera al azar y se guarda en /root/.sysfact-db.pass (solo root).
# Uso: sudo bash 02-postgres.sh
source "$(dirname "$0")/comun.sh"

paso "Instalando PostgreSQL"
DEBIAN_FRONTEND=noninteractive apt-get install -y postgresql postgresql-contrib
systemctl enable --now postgresql
PG_VER="$(ls /etc/postgresql | sort -V | tail -1)"
ok "PostgreSQL $PG_VER"
# Con varias versiones instaladas, psql y Sysfact usan la que escucha en 5432: tiene que ser esta.
PG_PUERTO="$(pg_lsclusters -h | awk -v v="$PG_VER" '$1==v && $2=="main" {print $3}')"
if [[ "$PG_PUERTO" != "5432" ]]; then
  echo "PostgreSQL $PG_VER escucha en el puerto $PG_PUERTO, no en 5432." >&2
  echo "Hay otra versión instalada (pg_lsclusters). Ejecutá antes 00-limpiar-previo.sh." >&2
  exit 1
fi

paso "Ajustes de memoria para un VPS de 4 GB"
cat > "/etc/postgresql/$PG_VER/main/conf.d/sysfact.conf" <<EOF
# Ajustes de Sysfact (VPS de 2 núcleos / 4 GB)
listen_addresses = 'localhost'
max_connections = 40
shared_buffers = 512MB
effective_cache_size = 1GB
work_mem = 8MB
maintenance_work_mem = 128MB
timezone = '$ZONA_HORARIA'
log_timezone = '$ZONA_HORARIA'
EOF
systemctl restart postgresql
ok "Solo escucha en localhost (no queda expuesto a internet)"

paso "Usuario y base de datos"
if [[ ! -s "$DB_PASS_FILE" ]]; then
  # Hexadecimal: sin caracteres que haya que escapar en la configuración de WildFly.
  openssl rand -hex 24 > "$DB_PASS_FILE"
  chmod 600 "$DB_PASS_FILE"
fi
DB_PASS="$(cat "$DB_PASS_FILE")"

if sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" | grep -q 1; then
  sudo -u postgres psql -q -c "ALTER ROLE \"$DB_USER\" WITH LOGIN PASSWORD '$DB_PASS';"
  ok "El usuario $DB_USER ya existía (contraseña actualizada)"
else
  sudo -u postgres psql -q -c "CREATE ROLE \"$DB_USER\" WITH LOGIN PASSWORD '$DB_PASS';"
  ok "Usuario $DB_USER creado"
fi

if sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1; then
  ok "La base $DB_NAME ya existía (no se tocó)"
else
  sudo -u postgres createdb -O "$DB_USER" -E UTF8 "$DB_NAME"
  ok "Base $DB_NAME creada (dueño: $DB_USER)"
fi

echo
echo "Paso 2 terminado. La base está vacía: cargala con el paso 'Cargar los datos' de la guía."
echo "Siguiente: sudo bash 03-wildfly.sh"
