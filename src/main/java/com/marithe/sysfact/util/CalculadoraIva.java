package com.marithe.sysfact.util;

import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.model.Venta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Calcula el IVA y los totales de una venta según la normativa paraguaya: los precios ya incluyen el
 * IVA, por lo que el impuesto se obtiene del importe de cada línea dividiendo por 11 (tasa 10 %) o
 * por 21 (tasa 5 %). Los montos se redondean a guaraníes enteros.
 *
 * <p>La tasa de cada línea la decide quien llama (la tasa del artículo). Los montos de IVA y los
 * totales que envía el cliente se ignoran y se recalculan.</p>
 *
 * <p>Resultado por línea: {@code iva10}/{@code iva5} = impuesto incluido en el importe;
 * {@code exenta} = importe exento. Resultado en la cabecera: la suma de cada columna, el total de
 * descuentos y el total de la factura.</p>
 */
public final class CalculadoraIva {

    public static final int TASA_10 = 10;
    public static final int TASA_5 = 5;
    public static final int EXENTA = 0;

    private static final BigDecimal ONCE = BigDecimal.valueOf(11);
    private static final BigDecimal VEINTIUNO = BigDecimal.valueOf(21);

    private CalculadoraIva() {}

    /**
     * Recalcula en el lugar los montos de cada línea y los totales de la venta.
     *
     * @param tasaDeLinea devuelve la tasa (10, 5 o 0) de cada línea, normalmente la de su artículo
     */
    public static void calcular(Venta venta, ToIntFunction<DetalleVenta> tasaDeLinea) {
        List<DetalleVenta> detalles = venta.getDetallesVenta();
        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessLogicException("La venta debe tener al menos un artículo.");
        }

        BigDecimal totalIva10 = BigDecimal.ZERO;
        BigDecimal totalIva5 = BigDecimal.ZERO;
        BigDecimal totalExenta = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;
        BigDecimal totalFactura = BigDecimal.ZERO;

        int linea = 0;
        for (DetalleVenta d : detalles) {
            linea++;
            int tasa = tasaDeLinea.applyAsInt(d);
            if (tasa != TASA_10 && tasa != TASA_5 && tasa != EXENTA) {
                throw new BusinessLogicException("La tasa de IVA de la línea " + linea + " no es válida: " + tasa);
            }

            if (d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new BusinessLogicException("La cantidad de la línea " + linea + " debe ser mayor a cero.");
            }
            BigDecimal precio = guaranies(d.getPrecioVenta());
            if (precio.signum() < 0) {
                throw new BusinessLogicException("El precio de la línea " + linea + " no puede ser negativo.");
            }
            BigDecimal descuento = guaranies(d.getDescuento());
            BigDecimal bruto = precio.multiply(BigDecimal.valueOf(d.getCantidad()));
            if (descuento.signum() < 0 || descuento.compareTo(bruto) > 0) {
                throw new BusinessLogicException(
                        "El descuento de la línea " + linea + " debe estar entre 0 y el importe de la línea.");
            }
            BigDecimal importe = bruto.subtract(descuento);

            d.setPrecioVenta(precio);
            d.setDescuento(descuento);
            d.setIva10(tasa == TASA_10 ? importe.divide(ONCE, 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            d.setIva5(tasa == TASA_5 ? importe.divide(VEINTIUNO, 0, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            d.setExenta(tasa == EXENTA ? importe : BigDecimal.ZERO);

            totalIva10 = totalIva10.add(d.getIva10());
            totalIva5 = totalIva5.add(d.getIva5());
            totalExenta = totalExenta.add(d.getExenta());
            totalDescuento = totalDescuento.add(descuento);
            totalFactura = totalFactura.add(importe);
        }

        venta.setTotalIva10(totalIva10);
        venta.setTotalIva5(totalIva5);
        venta.setTotalExenta(totalExenta);
        venta.setTotalDescuento(totalDescuento);
        venta.setTotalFactura(totalFactura);
    }

    private static BigDecimal guaranies(BigDecimal v) {
        return (v == null ? BigDecimal.ZERO : v).setScale(0, RoundingMode.HALF_UP);
    }
}
