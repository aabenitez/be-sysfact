-- Stock management: every change to an article's stock (goods received, inventory adjustments,
-- sales and their cancellations) is kept as a movement, and each article gets a minimum stock.
-- Run once on every existing database, BEFORE deploying the new backend:
--   sudo -u postgres psql -d sysfact -v app_user=sysfact -f migracion_stock_movimientos.sql
-- app_user is the database role the backend connects with (DB_USER in config.env). On a local
-- database where the backend connects as postgres, use -v app_user=postgres.

-- 1. Minimum stock: at or below it the article shows as "poco stock".
ALTER TABLE public.stock ADD COLUMN IF NOT EXISTS stock_minimo BIGINT NOT NULL DEFAULT 0;
ALTER TABLE public.stock DROP CONSTRAINT IF EXISTS ck_stock_minimo;
ALTER TABLE public.stock ADD CONSTRAINT ck_stock_minimo CHECK (stock_minimo >= 0);

-- Every article needs its stock row (older articles may not have one).
INSERT INTO public.stock (articulo_id, existencia)
SELECT a.id, 0 FROM public.articulo a
WHERE NOT EXISTS (SELECT 1 FROM public.stock s WHERE s.articulo_id = a.id);

-- 2. Movements. Rows are only ever added (the backend role gets SELECT and INSERT), so the history
-- explains every number. cantidad is the change (+ in, - out); existencia is the stock after it.
-- The user's name is copied, like in the audit log, so the row survives if the user is deleted.
CREATE TABLE IF NOT EXISTS public.movimiento_stock
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    articulo_id BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    cantidad BIGINT NOT NULL,
    existencia BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    venta_id BIGINT NULL,
    proveedor_id BIGINT NULL,
    motivo VARCHAR(250) NULL,
    CONSTRAINT pk_movimiento_stock PRIMARY KEY (id),
    CONSTRAINT ck_movimiento_stock_tipo
        CHECK (tipo IN ('INICIAL', 'INGRESO', 'AJUSTE', 'VENTA', 'DEVOLUCION')),
    -- Deleting an article (only possible when it has no sales) takes its history with it.
    CONSTRAINT fk_movimiento_stock_articulo_id FOREIGN KEY (articulo_id)
        REFERENCES public.articulo (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_stock_venta_id FOREIGN KEY (venta_id)
        REFERENCES public.venta (id),
    CONSTRAINT fk_movimiento_stock_proveedor_id FOREIGN KEY (proveedor_id)
        REFERENCES public.proveedor (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_movimiento_stock_articulo ON public.movimiento_stock (articulo_id, id DESC);

-- 3. Starting point of the history: today's stock of each article, so later movements add up.
INSERT INTO public.movimiento_stock (articulo_id, tipo, cantidad, existencia, usuario_nombre, motivo)
SELECT s.articulo_id, 'INICIAL', ROUND(s.existencia)::bigint, ROUND(s.existencia)::bigint, 'Sistema',
       'Existencia al comenzar el historial de stock'
FROM public.stock s
WHERE NOT EXISTS (SELECT 1 FROM public.movimiento_stock m WHERE m.articulo_id = s.articulo_id);

REVOKE ALL ON public.movimiento_stock FROM :"app_user";
GRANT SELECT, INSERT ON public.movimiento_stock TO :"app_user";
GRANT USAGE, SELECT ON SEQUENCE public.movimiento_stock_id_seq TO :"app_user";
