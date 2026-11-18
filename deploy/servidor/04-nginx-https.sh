#!/usr/bin/env bash
# Paso 4: instala nginx, que sirve el Angular compilado y pasa /sysfact a WildFly,
# y obtiene el certificado HTTPS gratuito de Let's Encrypt.
# Requisito: el dominio ya apunta a la IP de este VPS.
# Uso: sudo bash 04-nginx-https.sh
source "$(dirname "$0")/comun.sh"

paso "Instalando nginx y certbot"
DEBIAN_FRONTEND=noninteractive apt-get install -y nginx certbot python3-certbot-nginx
mkdir -p "$WEB_ROOT"
if [[ ! -f "$WEB_ROOT/index.html" ]]; then
  echo '<!doctype html><meta charset="utf-8"><title>Sysfact</title><p>Sysfact se está instalando.</p>' > "$WEB_ROOT/index.html"
fi
ok "nginx $(nginx -v 2>&1 | cut -d/ -f2)"

paso "Sitio $DOMINIO"
cat > /etc/nginx/sites-available/sysfact <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name $DOMINIO;

    root $WEB_ROOT;
    index index.html;

    # Logo de la empresa: hasta 2 MB (con margen).
    client_max_body_size 5m;

    gzip on;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;

    add_header X-Content-Type-Options nosniff always;
    add_header X-Frame-Options SAMEORIGIN always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    # API (Spring Boot en WildFly). "^~" le da prioridad sobre las reglas de archivos de abajo.
    location ^~ /sysfact/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 120s;
    }

    # JS y CSS de Angular llevan un hash en el nombre: cada versión es un archivo nuevo.
    location ~* \.(?:js|css)$ {
        try_files \$uri =404;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Imágenes y fuentes (algunas sin hash, como el logo por defecto): caché corta.
    location ~* \.(?:woff2?|ttf|svg|png|jpg|jpeg|gif|ico|webp)$ {
        try_files \$uri =404;
        expires 7d;
    }

    # Angular es una SPA: cualquier ruta (/inicio, /ventas/1...) carga index.html.
    location / {
        try_files \$uri \$uri/ /index.html;
        add_header Cache-Control "no-cache";
    }
}
EOF
ln -sfn /etc/nginx/sites-available/sysfact /etc/nginx/sites-enabled/sysfact
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl enable nginx >/dev/null 2>&1
systemctl restart nginx
ok "nginx configurado"

paso "Certificado HTTPS (Let's Encrypt)"
if certbot certificates 2>/dev/null | grep -q "Domains: .*$DOMINIO"; then
  ok "Ya hay un certificado para $DOMINIO"
else
  certbot --nginx -d "$DOMINIO" --redirect --agree-tos -m "$EMAIL_CERTBOT" --non-interactive
  ok "HTTPS activo; HTTP redirige a HTTPS"
fi
systemctl list-timers | grep -q certbot && ok "La renovación automática está programada"

echo
echo "Paso 4 terminado. Siguiente: sudo bash 05-backups.sh"
