package com.marithe.sysfact.util;

import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.DetalleVenta;
import com.marithe.sysfact.model.Venta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraIvaTest {

    /** Línea cuyo artículo tiene la tasa indicada (el id del artículo es la tasa, ver TASA). */
    private static DetalleVenta linea(long cantidad, long precio, long descuento, int tasa) {
        DetalleVenta d = new DetalleVenta();
        d.setCantidad(cantidad);
        d.setPrecioVenta(BigDecimal.valueOf(precio));
        d.setDescuento(BigDecimal.valueOf(descuento));
        // Montos que manda el cliente: se ignoran siempre.
        d.setIva10(BigDecimal.valueOf(2000));
        d.setIva5(BigDecimal.ZERO);
        d.setExenta(BigDecimal.ZERO);
        Articulo a = new Articulo();
        a.setId((long) tasa);
        a.setTasaIva(tasa);
        d.setArticulo(a);
        return d;
    }

    /** La tasa de cada línea es la de su artículo. */
    private static final ToIntFunction<DetalleVenta> TASA = d -> d.getArticulo().getTasaIva();

    private static Venta venta(DetalleVenta... lineas) {
        Venta v = new Venta();
        v.setDetallesVenta(new ArrayList<>(Arrays.asList(lineas)));
        return v;
    }

    private static void assertGs(long esperado, BigDecimal real) {
        assertEquals(0, BigDecimal.valueOf(esperado).compareTo(real), "esperado " + esperado + " y fue " + real);
    }

    @Test
    void iva10SeExtraeDelPrecioQueYaLoIncluye() {
        // Caso real de la venta 1: Gs. 20.000 al 10 % → IVA 1.818 (antes se guardaba 2.000).
        Venta v = venta(linea(1, 20000, 0, 10));
        CalculadoraIva.calcular(v, TASA);

        assertGs(1818, v.getDetallesVenta().get(0).getIva10());
        assertGs(1818, v.getTotalIva10());
        assertGs(0, v.getTotalIva5());
        assertGs(0, v.getTotalExenta());
        assertGs(20000, v.getTotalFactura());
    }

    @Test
    void iva5DividePor21() {
        Venta v = venta(linea(2, 10500, 0, 5)); // 21.000 al 5 % → 1.000
        CalculadoraIva.calcular(v, TASA);

        assertGs(1000, v.getTotalIva5());
        assertGs(0, v.getTotalIva10());
        assertGs(21000, v.getTotalFactura());
    }

    @Test
    void exentaGuardaElImporteSinImpuesto() {
        Venta v = venta(linea(3, 5000, 0, 0));
        CalculadoraIva.calcular(v, TASA);

        assertGs(15000, v.getTotalExenta());
        assertGs(0, v.getTotalIva10());
        assertGs(15000, v.getTotalFactura());
    }

    @Test
    void elDescuentoSeRestaAntesDeCalcularElIva() {
        Venta v = venta(linea(2, 15000, 8000, 10)); // 30.000 − 8.000 = 22.000 → IVA 2.000
        CalculadoraIva.calcular(v, TASA);

        assertGs(2000, v.getTotalIva10());
        assertGs(8000, v.getTotalDescuento());
        assertGs(22000, v.getTotalFactura());
    }

    @Test
    void ventaConTasasMezcladas() {
        Venta v = venta(
                linea(1, 20000, 0, 10),   // 10 % → 1.818
                linea(1, 10500, 0, 5),   //  5 % → 500
                linea(2, 3000, 1000, 0)  // exenta → 5.000
        );
        CalculadoraIva.calcular(v, TASA);

        assertGs(1818, v.getTotalIva10());
        assertGs(500, v.getTotalIva5());
        assertGs(5000, v.getTotalExenta());
        assertGs(1000, v.getTotalDescuento());
        assertGs(35500, v.getTotalFactura());
    }

    @Test
    void laTasaVieneDelArticuloYNoDeLoQueMarcaElCliente() {
        // El cliente marca iva10 en la línea, pero el artículo es exento: manda el artículo.
        Venta v = venta(linea(1, 20000, 0, 0));
        CalculadoraIva.calcular(v, TASA);

        assertGs(0, v.getTotalIva10());
        assertGs(20000, v.getTotalExenta());
    }

    @Test
    void seIgnoranLosTotalesQueMandaElCliente() {
        Venta v = venta(linea(1, 20000, 0, 10));
        v.setTotalIva10(BigDecimal.valueOf(999999));
        v.setTotalFactura(BigDecimal.ONE);
        CalculadoraIva.calcular(v, TASA);

        assertGs(1818, v.getTotalIva10());
        assertGs(20000, v.getTotalFactura());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(BusinessLogicException.class, () -> CalculadoraIva.calcular(venta(), TASA));
        assertThrows(BusinessLogicException.class,
                () -> CalculadoraIva.calcular(venta(linea(0, 1000, 0, 10)), TASA));
        assertThrows(BusinessLogicException.class,
                () -> CalculadoraIva.calcular(venta(linea(1, 1000, 2000, 10)), TASA));
        assertThrows(BusinessLogicException.class,
                () -> CalculadoraIva.calcular(venta(linea(1, 1000, 0, 10)), d -> 7));
    }
}
