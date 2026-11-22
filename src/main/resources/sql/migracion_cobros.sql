-- Accounts receivable: payments received for credit sales (venta.condicion_venta = 'CRE').
-- A credit sale is paid when its valid payments add up to its total.
-- Run once on every existing database, BEFORE deploying the new backend:
--   sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_cobros.sql
-- app_user is the database role the backend connects with (DB_USER in config.env). On a local
-- database where the backend connects as postgres, use -v app_user=postgres.

-- 1. Payments. A payment is never deleted or changed: a mistake is fixed by cancelling it (estado
-- ANULADO, with who, when and why), so the backend role may only update those columns.
-- The users' names are copied, like in the audit log, so the row survives if the user is deleted.
CREATE TABLE IF NOT EXISTS public.cobro
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    venta_id BIGINT NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    forma_pago VARCHAR(15) NOT NULL,
    referencia VARCHAR(100) NULL,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'VIGENTE',
    anulacion_fecha timestamp without time zone NULL,
    anulacion_usuario_id BIGINT NULL,
    anulacion_usuario_nombre VARCHAR(201) NULL,
    anulacion_motivo VARCHAR(250) NULL,
    CONSTRAINT pk_cobro PRIMARY KEY (id),
    CONSTRAINT ck_cobro_monto CHECK (monto > 0),
    CONSTRAINT ck_cobro_forma_pago
        CHECK (forma_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE', 'OTRO')),
    CONSTRAINT ck_cobro_estado CHECK (estado IN ('VIGENTE', 'ANULADO')),
    CONSTRAINT fk_cobro_venta_id FOREIGN KEY (venta_id) REFERENCES public.venta (id)
);

CREATE INDEX IF NOT EXISTS ix_cobro_venta ON public.cobro (venta_id);

-- 2. Credit term: a credit sale is due this many days after it was made (editable in Parámetros).
-- The sequence may lag behind rows inserted with explicit ids.
SELECT setval('public.parametros_id_seq', GREATEST((SELECT max(id) FROM public.parametros), 1));
INSERT INTO public.parametros (descripcion, codigo, activo, valor)
SELECT 'Plazo de las ventas a crédito (días)', 'credito.plazo.dias', true, '30'
WHERE NOT EXISTS (SELECT 1 FROM public.parametros WHERE codigo = 'credito.plazo.dias');

REVOKE ALL ON public.cobro FROM :"app_user";
GRANT SELECT, INSERT ON public.cobro TO :"app_user";
GRANT UPDATE (estado, anulacion_fecha, anulacion_usuario_id, anulacion_usuario_nombre, anulacion_motivo)
    ON public.cobro TO :"app_user";
GRANT USAGE, SELECT ON SEQUENCE public.cobro_id_seq TO :"app_user";
