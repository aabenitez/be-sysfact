#!/usr/bin/env bash
# Paso 5: copia de seguridad diaria de la base (02:30) y de los archivos subidos.
# Guarda $BACKUP_DIAS días en el VPS. Copialas también fuera del VPS (ver la guía).
# Uso: sudo bash 05-backups.sh
source "$(dirname "$0")/comun.sh"

paso "Script de copia: /usr/local/bin/sysfact-backup"
cat > /usr/local/bin/sysfact-backup <<EOF
#!/usr/bin/env bash
# Copia de seguridad de Sysfact: base de datos (formato custom de pg_dump) y archivos subidos.
set -euo pipefail
DIR="$BACKUP_DIR"
FECHA="\$(date +%Y-%m-%d_%H%M)"
mkdir -p "\$DIR"
sudo -u postgres pg_dump -Fc "$DB_NAME" > "\$DIR/sysfact_\$FECHA.dump"
tar -czf "\$DIR/uploads_\$FECHA.tar.gz" -C "$SYSFACT_DATOS" uploads
find "\$DIR" -type f -mtime +$BACKUP_DIAS -delete
echo "Copia creada: \$DIR/sysfact_\$FECHA.dump"
EOF
chmod 750 /usr/local/bin/sysfact-backup
mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"
ok "Creado"

paso "Tarea diaria a las 02:30"
echo "30 2 * * * root /usr/local/bin/sysfact-backup >> /var/log/sysfact-backup.log 2>&1" > /etc/cron.d/sysfact-backup
ok "Programada"

paso "Primera copia de prueba"
/usr/local/bin/sysfact-backup
ls -lh "$BACKUP_DIR" | tail -n +2

echo
echo "Paso 5 terminado. El servidor está listo: desplegá Sysfact con desplegar.sh desde tu máquina."
