#!/usr/bin/env bash
# Paso 3: instala Java 11 (Azul Zulu) y WildFly, configura el driver y el datasource de PostgreSQL
# y lo deja como servicio que escucha solo en 127.0.0.1 (nginx le pasa las peticiones).
# Uso: sudo bash 03-wildfly.sh
source "$(dirname "$0")/comun.sh"

WF_HOME="/opt/wildfly"
WF_DIR="/opt/wildfly-$WILDFLY_VERSION"

if [[ ! -s "$DB_PASS_FILE" ]]; then
  echo "No encuentro $DB_PASS_FILE. Ejecutá primero 02-postgres.sh." >&2
  exit 1
fi
DB_PASS="$(cat "$DB_PASS_FILE")"

paso "Instalando Java 11 (Azul Zulu)"
DEBIAN_FRONTEND=noninteractive apt-get install -y curl gpg ca-certificates
install -d -m 755 /etc/apt/keyrings
curl -fsSL https://repos.azul.com/azul-repo.key | gpg --dearmor --yes -o /etc/apt/keyrings/azul.gpg
echo "deb [signed-by=/etc/apt/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" \
  > /etc/apt/sources.list.d/zulu.list
# Si una ejecución anterior instaló Temurin, se quita para que quede un solo Java 11.
if dpkg -l 'temurin-11-*' 2>/dev/null | grep -q '^ii'; then
  DEBIAN_FRONTEND=noninteractive apt-get purge -y 'temurin-11-*'
fi
rm -f /etc/apt/sources.list.d/adoptium.list /etc/apt/keyrings/adoptium.gpg
apt-get update -y
DEBIAN_FRONTEND=noninteractive apt-get install -y zulu11-jre-headless \
  || DEBIAN_FRONTEND=noninteractive apt-get install -y zulu11-jdk-headless
ok "$(java -version 2>&1 | head -1)"

paso "Usuario de sistema wildfly"
id wildfly &>/dev/null || useradd --system --home-dir "$WF_HOME" --shell /usr/sbin/nologin wildfly
ok "Usuario wildfly"

paso "Descargando WildFly $WILDFLY_VERSION"
if [[ -d "$WF_DIR" ]]; then
  ok "Ya estaba instalado en $WF_DIR"
else
  TMP="$(mktemp -d)"
  curl -fL --retry 3 -o "$TMP/wildfly.tar.gz" \
    "https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz"
  tar -xzf "$TMP/wildfly.tar.gz" -C /opt
  rm -rf "$TMP"
  ok "Instalado en $WF_DIR"
fi
if [[ -d "$WF_HOME" && ! -L "$WF_HOME" ]]; then
  # Ya había otro WildFly instalado directamente en /opt/wildfly: se aparta (no se borra).
  systemctl stop wildfly 2>/dev/null || true
  APARTE="$WF_HOME-anterior-$(date +%Y%m%d%H%M%S)"
  mv "$WF_HOME" "$APARTE"
  echo "    Había otro WildFly en $WF_HOME; quedó apartado en $APARTE"
fi
ln -sfn "$WF_DIR" "$WF_HOME"

paso "Memoria y zona horaria de Java"
CONF="$WF_HOME/bin/standalone.conf"
sed -i "s|JBOSS_JAVA_SIZING=\"-Xms[^ ]* -Xmx[^ ]*|JBOSS_JAVA_SIZING=\"-Xms$WILDFLY_XMS -Xmx$WILDFLY_XMX|" "$CONF"
if ! grep -q 'user.timezone' "$CONF"; then
  echo "JAVA_OPTS=\"\$JAVA_OPTS -Duser.timezone=$ZONA_HORARIA -Dfile.encoding=UTF-8\"" >> "$CONF"
fi
ok "$(grep -o 'JBOSS_JAVA_SIZING="[^"]*"' "$CONF" | head -1)"

paso "Driver JDBC y datasource java:/jdbc/sysfactDS"
STANDALONE="$WF_HOME/standalone/configuration/standalone.xml"
if grep -q 'sysfactDS' "$STANDALONE"; then
  ok "El datasource ya estaba configurado (no se tocó)"
else
  TMP="$(mktemp -d)"
  curl -fL --retry 3 -o "$TMP/postgresql.jar" \
    "https://repo1.maven.org/maven2/org/postgresql/postgresql/$PG_JDBC_VERSION/postgresql-$PG_JDBC_VERSION.jar"
  cat > "$TMP/datasource.cli" <<EOF
embed-server --server-config=standalone.xml --std-out=discard
module add --name=org.postgresql --resources=$TMP/postgresql.jar --dependencies=javax.api,javax.transaction.api
/subsystem=datasources/jdbc-driver=postgres:add(driver-name=postgres,driver-module-name=org.postgresql,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)
data-source add --name=sysfactDS --jndi-name=java:/jdbc/sysfactDS --driver-name=postgres --connection-url=jdbc:postgresql://localhost:5432/$DB_NAME --user-name=$DB_USER --password=$DB_PASS --min-pool-size=2 --max-pool-size=20 --check-valid-connection-sql="SELECT 1" --background-validation=true --background-validation-millis=60000
stop-embedded-server
EOF
  JBOSS_HOME="$WF_HOME" "$WF_HOME/bin/jboss-cli.sh" --file="$TMP/datasource.cli"
  rm -rf "$TMP"
  ok "Datasource creado con el usuario $DB_USER"
fi

paso "Carpetas de datos de Sysfact ($SYSFACT_DATOS)"
mkdir -p "$SYSFACT_DATOS/uploads/logos" "$SYSFACT_DATOS/reportes"
chown -R wildfly:wildfly "$WF_DIR" "$SYSFACT_DATOS"
chmod -R u+rwX,g+rwX,o-rwx "$SYSFACT_DATOS"
# La contraseña de la base queda en standalone.xml: que solo la lea wildfly.
chmod 600 "$STANDALONE"
ok "uploads y reportes creados"

paso "Servicio systemd"
cat > /etc/systemd/system/wildfly.service <<EOF
[Unit]
Description=WildFly - Sysfact
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=wildfly
Group=wildfly
Environment=JBOSS_HOME=$WF_HOME
# Solo en 127.0.0.1: desde internet se entra por nginx (HTTPS).
ExecStart=$WF_HOME/bin/standalone.sh -b 127.0.0.1 -bmanagement 127.0.0.1
Restart=on-failure
RestartSec=10
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable wildfly
systemctl restart wildfly

echo -n "    Esperando a que WildFly arranque"
ARRANCO=no
for _ in $(seq 1 60); do
  if curl -s -o /dev/null http://127.0.0.1:8080/; then echo; ok "WildFly responde en 127.0.0.1:8080"; ARRANCO=si; break; fi
  echo -n "."; sleep 2
done
if [[ "$ARRANCO" != si ]]; then
  echo
  echo "    ✘ WildFly no respondió en 2 minutos. Revisá:" >&2
  echo "      systemctl status wildfly --no-pager" >&2
  echo "      tail -50 $WF_HOME/standalone/log/server.log" >&2
  echo "      ss -ltnp | grep -E ':(8080|9990)'" >&2
  exit 1
fi

echo
echo "Paso 3 terminado. Siguiente: sudo bash 04-nginx-https.sh"
