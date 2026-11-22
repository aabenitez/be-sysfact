#!/usr/bin/env bash
# Paso 5: copia de seguridad diaria de la base (02:30) y de los archivos subidos.
# Guarda $BACKUP_DIAS días en el VPS. Bajalas también a tu máquina con bajar-copias.sh (ver la guía).
# Se puede volver a ejecutar: reemplaza el script de copia y la tarea diaria.
# Uso: sudo bash 05-backups.sh
source "$(dirname "$0")/comun.sh"

paso "Script de copia: /usr/local/bin/sysfact-backup"
cat > /usr/local/bin/sysfact-backup <<EOF
#!/usr/bin/env bash
# Copia de seguridad de Sysfact: base de datos (formato custom de pg_dump) y archivos subidos.
# Cada copia se verifica antes de darla por buena; si algo falla, termina con error y no deja
# archivos a medias.
set -euo pipefail
# Las copias tienen datos de clientes y contraseñas cifradas: solo root las puede leer.
umask 077
DIR="$BACKUP_DIR"
FECHA="\$(date +%Y-%m-%d_%H%M)"
DUMP="\$DIR/sysfact_\$FECHA.dump"
TAR="\$DIR/uploads_\$FECHA.tar.gz"
mkdir -p "\$DIR"
trap 'rm -f "\$DUMP.tmp" "\$TAR.tmp"' EXIT

sudo -u postgres pg_dump -Fc "$DB_NAME" > "\$DUMP.tmp"
# Una copia que pg_restore no puede leer no sirve para nada.
pg_restore --list "\$DUMP.tmp" > /dev/null
mv "\$DUMP.tmp" "\$DUMP"

tar -czf "\$TAR.tmp" -C "$SYSFACT_DATOS" uploads
tar -tzf "\$TAR.tmp" > /dev/null
mv "\$TAR.tmp" "\$TAR"

find "\$DIR" -type f \( -name 'sysfact_*.dump' -o -name 'uploads_*.tar.gz' \) -mtime +$BACKUP_DIAS -delete
echo "\$(date '+%Y-%m-%d %H:%M') Copia creada y verificada: \$DUMP (\$(du -h "\$DUMP" | cut -f1))"
EOF
chmod 750 /usr/local/bin/sysfact-backup
mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"
# Las copias anteriores a este cambio quedaron legibles para todos.
find "$BACKUP_DIR" -type f -exec chmod 600 {} +
ok "Creado"

paso "Tarea diaria a las 02:30"
echo "30 2 * * * root /usr/local/bin/sysfact-backup >> /var/log/sysfact-backup.log 2>&1" > /etc/cron.d/sysfact-backup
touch /var/log/sysfact-backup.log
chmod 640 /var/log/sysfact-backup.log
ok "Programada"

paso "Copia de prueba"
/usr/local/bin/sysfact-backup
ls -lh "$BACKUP_DIR" | tail -n 4

echo
echo "Paso 5 terminado. Para tener las copias también fuera del VPS, usá bajar-copias.sh desde tu máquina."
