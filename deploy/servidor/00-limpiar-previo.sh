#!/usr/bin/env bash
# Paso 0 (solo si el VPS NO está recién instalado): apaga lo que trajo de antes y choca con Sysfact.
# - Apache (ocupa el puerto 80 que necesita nginx)
# - Firewall CSF/lfd (se queda solo ufw)
# - sendmail y postfix-mta-sts-resolver (Sysfact manda correos por el servidor SMTP externo)
# - PostgreSQL viejos: la versión más nueva pasa al puerto 5432 y las otras se detienen
# - Un WildFly instalado directamente en /opt/wildfly (se aparta, 03-wildfly.sh instala el suyo)
# No borra datos ni desinstala paquetes: solo detiene y deshabilita servicios.
# Uso: sudo bash 00-limpiar-previo.sh
source "$(dirname "$0")/comun.sh"

apagar() {
  local s
  for s in "$@"; do
    if systemctl list-unit-files "$s.service" --no-legend 2>/dev/null | grep -q .; then
      systemctl disable --now "$s" >/dev/null 2>&1 || true
      ok "$s detenido y deshabilitado"
    fi
  done
}

paso "Servicios que no usa Sysfact"
apagar apache2 sendmail postfix-mta-sts-resolver

paso "Firewall: se deja solo ufw"
if command -v csf >/dev/null; then
  csf -x >/dev/null 2>&1 || true
fi
apagar lfd csf
ufw --force enable >/dev/null
ufw reload >/dev/null
ok "ufw activo: $(ufw status | grep -cE 'ALLOW') reglas (SSH $SSH_PUERTO, 80, 443)"

paso "PostgreSQL: una sola versión, en el puerto 5432"
if command -v pg_lsclusters >/dev/null; then
  PG_NUEVA="$(ls /etc/postgresql | sort -V | tail -1)"
  while read -r ver cluster puerto _; do
    [[ "$ver" == "$PG_NUEVA" && "$cluster" == "main" ]] && continue
    pg_ctlcluster "$ver" "$cluster" stop 2>/dev/null || true
    systemctl disable "postgresql@$ver-$cluster" >/dev/null 2>&1 || true
    # Se le cambia el puerto para que nunca vuelva a ocupar el 5432 (sus datos quedan intactos).
    if [[ "$puerto" == "5432" ]]; then
      pg_conftool "$ver" "$cluster" set port 5499
    fi
    echo "    PostgreSQL $ver/$cluster detenido (sus datos siguen en /var/lib/postgresql/$ver)"
  done < <(pg_lsclusters -h)
  pg_conftool "$PG_NUEVA" main set port 5432
  pg_ctlcluster "$PG_NUEVA" main restart
  systemctl enable "postgresql@$PG_NUEVA-main" >/dev/null 2>&1 || true
  ok "PostgreSQL $PG_NUEVA en el puerto 5432"
fi

paso "WildFly instalado de antes en /opt/wildfly"
if [[ -d /opt/wildfly && ! -L /opt/wildfly ]]; then
  systemctl stop wildfly 2>/dev/null || true
  APARTE="/opt/wildfly-anterior-$(date +%Y%m%d%H%M%S)"
  mv /opt/wildfly "$APARTE"
  ok "Apartado en $APARTE (03-wildfly.sh instala el de Sysfact)"
else
  ok "No había"
fi

echo
echo "Paso 0 terminado. Siguiente: volvé a ejecutar 02-postgres.sh, 03-wildfly.sh y 04-nginx-https.sh"
