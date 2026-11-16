-- Corrige el IVA de la venta 1, cargada antes de que el backend calculara el IVA.
-- Gs. 20.000 con IVA 10 % incluido -> IVA = 20.000 / 11 = 1.818 (se había guardado 2.000).
-- Solo toca la venta 1 y solo si todavía tiene el valor incorrecto; se puede ejecutar más de una vez.

BEGIN;

UPDATE detalle_venta
   SET iva10 = ROUND((cantidad * precio_venta - descuento) / 11)
 WHERE venta_id = 1
   AND iva10 > 0
   AND iva10 <> ROUND((cantidad * precio_venta - descuento) / 11);

UPDATE venta v
   SET total_iva10 = (SELECT COALESCE(SUM(d.iva10), 0) FROM detalle_venta d WHERE d.venta_id = v.id)
 WHERE v.id = 1;

SELECT v.id, v.total_iva10, v.total_factura, d.id AS detalle_id, d.iva10
  FROM venta v
  JOIN detalle_venta d ON d.venta_id = v.id
 WHERE v.id = 1;

COMMIT;
