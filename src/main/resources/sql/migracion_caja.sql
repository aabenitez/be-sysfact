-- Cash count: how each cash sale was paid, and the daily cash closings.
-- Run once on every existing database, BEFORE deploying the new backend:
--   sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_caja.sql
-- app_user is the database role the backend connects with (DB_USER in config.env). On a local
-- database where the backend connects as postgres, use -v app_user=postgres.

-- 1. Payment method of cash sales (credit sales are paid through cobro, so theirs stays empty).
-- Sales made before this change were taken as cash.
ALTER TABLE public.venta ADD COLUMN IF NOT EXISTS forma_pago VARCHAR(15) NULL;
UPDATE public.venta SET forma_pago = 'EFECTIVO' WHERE condicion_venta = 'CON' AND forma_pago IS NULL;
UPDATE public.venta SET forma_pago = NULL WHERE condicion_venta = 'CRE' AND forma_pago IS NOT NULL;
ALTER TABLE public.venta DROP CONSTRAINT IF EXISTS ck_venta_forma_pago;
ALTER TABLE public.venta ADD CONSTRAINT ck_venta_forma_pago CHECK (
    (condicion_venta = 'CRE' AND forma_pago IS NULL)
    OR (condicion_venta = 'CON'
        AND forma_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE', 'OTRO')));

-- 2. Cash closings: at the end of the day someone counts the cash in the drawer and the system
-- compares it with what should be there (opening float + cash sales + cash payments of the day).
-- Like payments, a closing is never changed or deleted: a wrong one is cancelled (by an
-- administrator, with a reason) and the day is closed again. Only one valid closing per day.
CREATE TABLE IF NOT EXISTS public.caja_cierre
(
    id BIGSERIAL,
    fecha DATE NOT NULL,
    fondo_inicial DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_ventas DECIMAL(19,2) NOT NULL,
    total_cobros DECIMAL(19,2) NOT NULL,
    efectivo_esperado DECIMAL(19,2) NOT NULL,
    efectivo_contado DECIMAL(19,2) NOT NULL,
    diferencia DECIMAL(19,2) NOT NULL,
    observacion VARCHAR(250) NULL,
    creado timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'VIGENTE',
    anulacion_fecha timestamp without time zone NULL,
    anulacion_usuario_id BIGINT NULL,
    anulacion_usuario_nombre VARCHAR(201) NULL,
    anulacion_motivo VARCHAR(250) NULL,
    CONSTRAINT pk_caja_cierre PRIMARY KEY (id),
    CONSTRAINT ck_caja_cierre_montos CHECK (fondo_inicial >= 0 AND efectivo_contado >= 0),
    CONSTRAINT ck_caja_cierre_estado CHECK (estado IN ('VIGENTE', 'ANULADO'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_caja_cierre_fecha_vigente
    ON public.caja_cierre (fecha) WHERE estado = 'VIGENTE';
-- The day's movements are looked up by date.
CREATE INDEX IF NOT EXISTS ix_cobro_fecha ON public.cobro (fecha);
CREATE INDEX IF NOT EXISTS ix_venta_fecha ON public.venta (fecha);

REVOKE ALL ON public.caja_cierre FROM :"app_user";
GRANT SELECT, INSERT ON public.caja_cierre TO :"app_user";
GRANT UPDATE (estado, anulacion_fecha, anulacion_usuario_id, anulacion_usuario_nombre, anulacion_motivo)
    ON public.caja_cierre TO :"app_user";
GRANT USAGE, SELECT ON SEQUENCE public.caja_cierre_id_seq TO :"app_user";
