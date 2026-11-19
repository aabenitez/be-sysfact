-- Sales can be cancelled (anuladas) instead of deleted, and every change to sales and clients is
-- written to an audit log.
-- Run once on every existing database, BEFORE deploying the new backend:
--   sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_venta_anulacion_auditoria.sql
-- app_user is the database role the backend connects with (DB_USER in config.env). On a local
-- database where the backend connects as postgres, use -v app_user=postgres.

-- 1. Cancelling a sale keeps it (and its number) with who cancelled it, when and why.
ALTER TABLE public.venta ADD COLUMN IF NOT EXISTS estado VARCHAR(10) NOT NULL DEFAULT 'EMITIDA';
ALTER TABLE public.venta ADD COLUMN IF NOT EXISTS anulacion_fecha timestamp without time zone NULL;
ALTER TABLE public.venta ADD COLUMN IF NOT EXISTS anulacion_usuario_id BIGINT NULL;
ALTER TABLE public.venta ADD COLUMN IF NOT EXISTS anulacion_motivo VARCHAR(250) NULL;

ALTER TABLE public.venta DROP CONSTRAINT IF EXISTS venta_estado_check;
ALTER TABLE public.venta ADD CONSTRAINT venta_estado_check
    CHECK (estado IN ('EMITIDA', 'ANULADA'));

ALTER TABLE public.venta DROP CONSTRAINT IF EXISTS fk_venta_anulacion_usuario_id;
ALTER TABLE public.venta ADD CONSTRAINT fk_venta_anulacion_usuario_id
    FOREIGN KEY (anulacion_usuario_id) REFERENCES public.usuario (id);

-- 2. Audit log. Rows are only ever added: the backend role gets SELECT and INSERT, not UPDATE or
-- DELETE, so not even a bug in the application can rewrite the history.
-- usuario_id has no foreign key and the name is copied, so the entry survives if the user is
-- deleted later.
CREATE TABLE IF NOT EXISTS public.auditoria
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    usuario_rol VARCHAR(50) NULL,
    accion VARCHAR(20) NOT NULL,
    entidad VARCHAR(30) NOT NULL,
    entidad_id BIGINT NULL,
    detalle VARCHAR(500) NULL,
    datos_antes JSONB NULL,
    datos_despues JSONB NULL,
    CONSTRAINT pk_auditoria PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_auditoria_entidad ON public.auditoria (entidad, entidad_id);
CREATE INDEX IF NOT EXISTS ix_auditoria_fecha ON public.auditoria (fecha DESC);

-- The owner of the table is postgres; the backend role may only read and add rows.
REVOKE ALL ON public.auditoria FROM :"app_user";
GRANT SELECT, INSERT ON public.auditoria TO :"app_user";
GRANT USAGE, SELECT ON SEQUENCE public.auditoria_id_seq TO :"app_user";
