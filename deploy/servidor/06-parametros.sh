#!/usr/bin/env bash
# Paso 6 (después de cargar los datos): ajusta los parámetros del sistema al servidor.
# Uso: sudo bash 06-parametros.sh
source "$(dirname "$0")/comun.sh"

psql_sysfact() { sudo -u postgres psql -d "$DB_NAME" -v ON_ERROR_STOP=1 -q "$@"; }

paso "Rutas de archivos y enlace de cambio de contraseña"
psql_sysfact <<EOF
UPDATE parametros SET valor = '$SYSFACT_DATOS/uploads'  WHERE codigo = 'uploads.path';
UPDATE parametros SET valor = '$SYSFACT_DATOS/reportes' WHERE codigo = 'reports.path';
UPDATE parametros SET valor = 'https://$DOMINIO/cambiar-contrasena?token=' WHERE codigo = 'url.change.password';
EOF
psql_sysfact -c "SELECT codigo, valor FROM parametros WHERE codigo IN ('uploads.path','reports.path','url.change.password') ORDER BY codigo;"

paso "Permisos del usuario $DB_USER sobre las tablas"
# Si los datos se restauraron con otro dueño, el usuario de la aplicación igual necesita acceso.
psql_sysfact <<EOF
GRANT USAGE ON SCHEMA public TO "$DB_USER";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO "$DB_USER";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO "$DB_USER";
-- The audit log is append-only: the application may read and add entries, never change them.
REVOKE UPDATE, DELETE, TRUNCATE ON public.auditoria FROM "$DB_USER";
-- Stock movements too: the history of every stock number cannot be rewritten.
REVOKE UPDATE, DELETE, TRUNCATE ON public.movimiento_stock FROM "$DB_USER";
-- Payments are never deleted or rewritten: the application may only cancel them.
REVOKE UPDATE, DELETE, TRUNCATE ON public.cobro FROM "$DB_USER";
GRANT UPDATE (estado, anulacion_fecha, anulacion_usuario_id, anulacion_usuario_nombre, anulacion_motivo)
    ON public.cobro TO "$DB_USER";
-- Cash closings likewise: only cancelled, never rewritten.
REVOKE UPDATE, DELETE, TRUNCATE ON public.caja_cierre FROM "$DB_USER";
GRANT UPDATE (estado, anulacion_fecha, anulacion_usuario_id, anulacion_usuario_nombre, anulacion_motivo)
    ON public.caja_cierre TO "$DB_USER";
EOF
ok "Permisos otorgados"

paso "Reiniciando WildFly para que lea los parámetros nuevos"
systemctl restart wildfly
ok "Listo"

echo
echo "Revisá a mano la contraseña del correo (parámetro email.password) desde la pantalla Parámetros"
echo "o con psql: no se cambia automáticamente."
