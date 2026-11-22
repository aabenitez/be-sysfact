# Despliegue de Sysfact en un VPS

Guía para instalar Sysfact (backend + frontend + base de datos) en un VPS con
**Debian 12** (o Ubuntu 24.04), 2 núcleos y 4 GB de RAM.

```
Internet ──HTTPS──▶ nginx (443) ──┬─▶ /            Angular compilado (/var/www/sysfact)
                                  └─▶ /sysfact/    WildFly 26 en 127.0.0.1:8080 ──▶ PostgreSQL (localhost)
```

- Desde internet solo quedan abiertos los puertos **22 (SSH), 80 y 443**.
  WildFly (8080, consola 9990) y PostgreSQL (5432) escuchan solo dentro del VPS.
- Se compila **en tu máquina** y al VPS solo se sube el resultado (`sysfact.war` y el Angular
  compilado). Así el servidor no gasta memoria compilando.

| Archivo | Dónde se ejecuta | Qué hace |
|---|---|---|
| `servidor/00-limpiar-previo.sh` | VPS | Solo si el VPS no venía limpio: apaga Apache, CSF, PostgreSQL y WildFly anteriores |
| `servidor/01-base.sh` | VPS | Actualiza el sistema, zona horaria, firewall, swap, fail2ban |
| `servidor/02-postgres.sh` | VPS | PostgreSQL, base `sysfact` y usuario propio con contraseña al azar |
| `servidor/03-wildfly.sh` | VPS | Java 11 (Azul Zulu), WildFly 26.1.3, driver y datasource `java:/jdbc/sysfactDS`, servicio |
| `servidor/04-nginx-https.sh` | VPS | nginx + certificado HTTPS de Let's Encrypt |
| `servidor/05-backups.sh` | VPS | Copia de seguridad diaria (02:30) de la base y del logo |
| `copiar-base.sh` | Tu máquina (WSL) | Copia tu base local al VPS (una sola vez) |
| `servidor/06-parametros.sh` | VPS | Ajusta rutas y enlaces de la tabla `parametros` al servidor |
| `desplegar.sh` | Tu máquina (WSL) | Compila y publica una versión nueva (cada vez que hay cambios) |
| `bajar-copias.sh` | Tu máquina (WSL) | Baja las copias de seguridad del VPS a tu máquina |

---

## 0. Antes de empezar

1. **Dominio:** creá un registro DNS tipo **A** que apunte a la IP del VPS
   (por ejemplo `sysfact.tudominio.com.py`). Verificalo desde tu máquina:
   ```bash
   ping -c1 sysfact.tudominio.com.py
   ```
   Tiene que responder la IP del VPS. Sin esto, el paso 4 (HTTPS) falla.

2. **Acceso SSH con clave** (recomendado, evita escribir la contraseña en cada despliegue). En WSL:
   ```bash
   ssh-keygen -t ed25519
   ```
   ```bash
   ssh-copy-id usuario@IP_DEL_VPS
   ```
   El usuario del VPS tiene que poder usar `sudo`.

3. **Configuración:** en WSL, dentro de `be-sysfact/deploy`, copiá el ejemplo y completalo:
   ```bash
   cp config.env.example config.env
   ```
   ```bash
   nano config.env
   ```
   Lo mínimo: `SERVIDOR`, `DOMINIO` y `EMAIL_CERTBOT`. `config.env` no se sube a git.

## 1. Subir los scripts al VPS

Desde WSL, en `be-sysfact`:

```bash
scp -r deploy usuario@IP_DEL_VPS:~/
```

Entrá al VPS:

```bash
ssh usuario@IP_DEL_VPS
```

## 2. Preparar el servidor (en el VPS)

Ejecutalos en orden. Cada uno muestra lo que hizo y cuál sigue; se pueden volver a ejecutar sin romper nada.

```bash
cd ~/deploy/servidor
```

```bash
sudo bash 01-base.sh
```

```bash
sudo bash 02-postgres.sh
```

```bash
sudo bash 03-wildfly.sh
```

```bash
sudo bash 04-nginx-https.sh
```

```bash
sudo bash 05-backups.sh
```

Al terminar, `https://TU_DOMINIO` ya responde con HTTPS (todavía con una página de "instalando").

> La contraseña del usuario `sysfact` de la base se genera al azar y queda en
> `/root/.sysfact-db.pass` (solo root la puede leer). WildFly la tiene en su
> `standalone.xml`, que también queda con permisos solo para `wildfly`.

## 3. Cargar los datos

**Opción A — copiar tu base local (recomendada).** Lleva usuarios, roles, parámetros,
artículos, clientes y ventas tal como están en tu máquina. Desde WSL, en `be-sysfact/deploy`:

```bash
bash copiar-base.sh
```

Pide confirmación (escribí `SI`), tu contraseña de sudo local y la del VPS.

> Ojo: tu base local tiene datos de prueba (por ejemplo la venta 1 y el cliente
> "Nombre de Prueba 33"). Borralos desde el sistema una vez publicado, o limpiá la base
> local antes de copiarla.

**Opción B — base vacía desde los scripts del proyecto.** En el VPS:

```bash
sudo -u postgres psql -d sysfact -f ~/sysfact.sql
```

(subí antes `src/main/resources/sql/sysfact.sql` con `scp`). Ese script ya incluye la columna
`tasa_iva` y datos iniciales; revisá los usuarios y parámetros que trae.

**Después de cualquiera de las dos**, en el VPS:

```bash
sudo bash ~/deploy/servidor/06-parametros.sh
```

Ajusta `uploads.path`, `reports.path` y `url.change.password` al servidor y da permisos al usuario `sysfact`.

## 4. Publicar Sysfact

Desde WSL, en `be-sysfact/deploy`:

```bash
bash desplegar.sh
```

Compila el backend y el frontend, los sube y los instala. Al final muestra `Listo: https://TU_DOMINIO`.
Entrá y verificá:

- [ ] Abre la pantalla de inicio de sesión con HTTPS (candado en el navegador).
- [ ] Iniciás sesión con tu usuario.
- [ ] El Inicio muestra los números de ventas, clientes, artículos, etc.
- [ ] **Subí el logo de nuevo** en Parámetros → Logo de la empresa (el archivo de tu máquina no se copia).
      Ahí mismo podés subir el ícono de la pestaña (favicon).
- [ ] Probá crear y borrar un artículo de prueba.

## 5. Actualizar a una versión nueva

Cada vez que haya cambios (después de `git pull`), desde WSL en `be-sysfact/deploy`:

```bash
bash desplegar.sh
```

Para subir solo una parte:

```bash
bash desplegar.sh backend
```

```bash
bash desplegar.sh frontend
```

Al desplegar el backend, WildFly se reinicia (el sistema queda sin servicio entre 30 segundos y un
minuto, y los usuarios tienen que volver a iniciar sesión). No se redespliega "en caliente" porque cada
redespliegue de Spring Boot deja clases en memoria (Metaspace) y, después de unos cuantos, WildFly
falla con `OutOfMemoryError: Metaspace` y el login responde 404.

Si la versión nueva del backend cambia la base (un script en `src/main/resources/sql/migracion_*.sql`),
ejecutalo en el VPS **antes** de desplegar:

```bash
sudo -u postgres psql -d sysfact -f migracion_xxx.sql
```

La migración `migracion_venta_anulacion_auditoria.sql` (anulación de ventas y registro de auditoría)
necesita además el usuario de la base con el que se conecta el backend (`DB_USER` de `config.env`),
porque le da permiso de leer y agregar en `auditoria`, pero no de modificar ni borrar:

```bash
sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_venta_anulacion_auditoria.sql
```

Lo mismo con `migracion_stock_movimientos.sql` (historial de stock y stock mínimo):

```bash
sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_stock_movimientos.sql
```

**Volver atrás el frontend:** la versión anterior queda en `/var/www/sysfact.anterior`.

```bash
sudo mv /var/www/sysfact /var/www/sysfact.malo && sudo mv /var/www/sysfact.anterior /var/www/sysfact
```

## 6. Copias de seguridad

- Se hacen solas todos los días a las 02:30 en `/var/backups/sysfact` (se guardan 14 días).
  Cada copia se verifica con `pg_restore --list` y solo la puede leer root.
- Copia manual en cualquier momento: `sudo sysfact-backup`
- Resultado de cada copia: `sudo tail /var/log/sysfact-backup.log`
- **Importante:** una copia que solo está en el VPS se pierde si se pierde el VPS.
  Bajalas a tu máquina. Desde WSL, en `be-sysfact/deploy`:
  ```bash
  bash bajar-copias.sh
  ```
  Baja solo las copias nuevas a `BACKUP_LOCAL` (por defecto `~/backups-sysfact`), las guarda
  90 días, verifica la última y avisa si tiene más de 36 horas. Si `BACKUP_LOCAL` está dentro
  de OneDrive o Google Drive, las copias quedan además en la nube.

  **Que se baje solo todos los días (Windows):** en PowerShell, una sola vez
  (cambiá la ruta si tu repositorio está en otro lado):
  ```powershell
  schtasks /Create /SC DAILY /ST 09:00 /TN "Sysfact - bajar copias" /TR "wsl.exe -d Ubuntu-24.04 -- bash -lc 'cd /mnt/c/Users/User/OneDrive/Documentos/repo/be-sysfact/deploy && bash bajar-copias.sh >> ~/backups-sysfact.log 2>&1'"
  ```
  Si la PC está apagada a esa hora, la copia se baja la próxima vez que corra; las del VPS
  duran 14 días.

**Restaurar** una copia (en el VPS):

```bash
sudo systemctl stop wildfly
```

```bash
sudo -u postgres dropdb sysfact && sudo -u postgres createdb -O sysfact sysfact
```

```bash
sudo -u postgres pg_restore --no-owner --role=sysfact -d sysfact /var/backups/sysfact/sysfact_FECHA.dump
```

```bash
sudo systemctl start wildfly
```

## 7. Comandos útiles (en el VPS)

| Para | Comando |
|---|---|
| Ver si WildFly está corriendo | `sudo systemctl status wildfly` |
| Log de WildFly en vivo | `sudo tail -f /opt/wildfly/standalone/log/server.log` |
| Reiniciar WildFly | `sudo systemctl restart wildfly` |
| Log de nginx (errores) | `sudo tail -f /var/log/nginx/error.log` |
| Entrar a la base | `sudo -u postgres psql -d sysfact` |
| Memoria usada | `free -h` |
| Estado del firewall | `sudo ufw status` |
| Vencimiento del certificado | `sudo certbot certificates` |

## 8. Pendientes de seguridad antes de usarlo en serio

- [ ] **Contraseña del correo:** el parámetro `email.password` está en texto plano en la base.
      Usá una contraseña de aplicación exclusiva para Sysfact.
- [ ] **Contraseñas de los usuarios del sistema:** cambiá las de prueba (por ejemplo `12345`).
- [x] **Recuperar contraseña:** la pantalla de inicio de sesión tiene "¿Olvidaste tu contraseña?". El correo
      lleva a `https://TU_DOMINIO/cambiar-contrasena?token=…` (vale 1 hora, un solo uso). Probalo una vez
      con tu cuenta para confirmar que el VPS puede enviar correos.
- [x] **Clave JWT:** ya no está en el código. Si no se configura, WildFly genera una al azar en cada arranque
      (al reiniciarlo hay que volver a iniciar sesión). Para fijarla, agregá en `/opt/wildfly/bin/standalone.conf`:
      `JAVA_OPTS="$JAVA_OPTS -Dsysfact.jwt.secret=CLAVE"`, donde CLAVE es la salida de
      `openssl rand -base64 64 | tr -d '\n'`.
