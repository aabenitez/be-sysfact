-- Agrega la tasa de IVA a cada artículo (10, 5 o 0 = exenta) y asegura que todos tengan fila de stock.
-- Los artículos existentes quedan con 10 %, que es como se venían vendiendo.
-- Se puede ejecutar más de una vez.

BEGIN;

ALTER TABLE public.articulo
    ADD COLUMN IF NOT EXISTS tasa_iva SMALLINT NOT NULL DEFAULT 10;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_articulo_tasa_iva') THEN
        ALTER TABLE public.articulo
            ADD CONSTRAINT ck_articulo_tasa_iva CHECK (tasa_iva IN (0, 5, 10));
    END IF;
END $$;

-- Cada venta descuenta stock: los artículos sin fila de stock no lo registraban.
INSERT INTO public.stock (articulo_id, existencia)
SELECT a.id, 0
  FROM public.articulo a
 WHERE NOT EXISTS (SELECT 1 FROM public.stock s WHERE s.articulo_id = a.id);

SELECT a.id, a.nombre, a.tasa_iva, s.existencia
  FROM public.articulo a
  LEFT JOIN public.stock s ON s.articulo_id = a.id
 ORDER BY a.id;

COMMIT;
