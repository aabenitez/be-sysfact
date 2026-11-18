#!/usr/bin/env bash
# Compila Sysfact en tu máquina y lo sube al VPS (backend + frontend).
# Se ejecuta desde WSL, en la carpeta deploy:  bash desplegar.sh
#   bash desplegar.sh            despliega backend y frontend
#   bash desplegar.sh backend    solo el .war
#   bash desplegar.sh frontend   solo el Angular
# Te va a pedir la contraseña de sudo del VPS una vez.
set -euo pipefail

AQUI="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=/dev/null
source "$AQUI/config.env"

QUE="${1:-todo}"
SSH=(ssh -p "$SSH_PUERTO")
SCP=(scp -P "$SSH_PUERTO")
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

if [[ "$QUE" == "todo" || "$QUE" == "backend" ]]; then
  echo "==> Compilando el backend (Maven)"
  (cd "$BACKEND_DIR" && mvn -B -q -DskipTests clean package)
  cp "$BACKEND_DIR/target/sysfact.war" "$TMP/"
  echo "    ✔ sysfact.war ($(du -h "$TMP/sysfact.war" | cut -f1))"
fi

if [[ "$QUE" == "todo" || "$QUE" == "frontend" ]]; then
  echo "==> Compilando el frontend (Angular, producción)"
  (cd "$FRONTEND_DIR" && NODE_OPTIONS=--max-old-space-size=3072 npx ng build --configuration production)
  tar -czf "$TMP/frontend.tar.gz" -C "$FRONTEND_DIR/dist/Matdash" .
  echo "    ✔ frontend.tar.gz ($(du -h "$TMP/frontend.tar.gz" | cut -f1))"
fi

echo "==> Subiendo al VPS ($SERVIDOR)"
"${SSH[@]}" "$SERVIDOR" "rm -rf /tmp/sysfact-deploy && mkdir -p /tmp/sysfact-deploy"
"${SCP[@]}" "$TMP"/* "$SERVIDOR:/tmp/sysfact-deploy/"

echo "==> Instalando en el VPS"
"${SSH[@]}" "$SERVIDOR" "sudo bash -s" <<EOF
set -euo pipefail
cd /tmp/sysfact-deploy

if [[ -f frontend.tar.gz ]]; then
  rm -rf "$WEB_ROOT.nuevo" && mkdir -p "$WEB_ROOT.nuevo"
  tar -xzf frontend.tar.gz -C "$WEB_ROOT.nuevo"
  rm -rf "$WEB_ROOT.anterior"
  [[ -d "$WEB_ROOT" ]] && mv "$WEB_ROOT" "$WEB_ROOT.anterior"
  mv "$WEB_ROOT.nuevo" "$WEB_ROOT"
  chown -R root:root "$WEB_ROOT" && chmod -R a+rX "$WEB_ROOT"
  echo "    ✔ Frontend publicado (la versión anterior quedó en $WEB_ROOT.anterior)"
fi

if [[ -f sysfact.war ]]; then
  DEP=/opt/wildfly/standalone/deployments
  rm -f "\$DEP"/sysfact.war.{deployed,failed,undeployed}
  install -o wildfly -g wildfly -m 640 sysfact.war "\$DEP/sysfact.war.tmp"
  mv "\$DEP/sysfact.war.tmp" "\$DEP/sysfact.war"
  echo -n "    Desplegando el backend"
  for _ in \$(seq 1 90); do
    if [[ -f "\$DEP/sysfact.war.deployed" ]]; then echo; echo "    ✔ Backend desplegado"; break; fi
    if [[ -f "\$DEP/sysfact.war.failed" ]]; then
      echo; echo "    ✘ Falló el despliegue:"; cat "\$DEP/sysfact.war.failed"; exit 1
    fi
    echo -n "."; sleep 2
  done
fi

rm -rf /tmp/sysfact-deploy
EOF

echo
echo "Listo: https://$DOMINIO"
