package com.marithe.sysfact.model;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class DetalleVenta extends BaseEntity {

    @NotNull(message = "La cantidad no puede estar nula")
    private Long cantidad;

    @NotNull(message = "El descuento no puede estar nulo")
    private BigDecimal descuento;

    private BigDecimal exenta;

    @NotNull(message = "El cálculo de IVA 10% no puede estar nulo")
    private BigDecimal iva10;

    @NotNull(message = "El cálculo de IVA 5% no puede estar nulo")
    private BigDecimal iva5;

    @NotNull(message = "El precio de venta no puede estar nulo")
    private BigDecimal precioVenta;

    private Articulo articulo;

    // Constructores
    public DetalleVenta() {}

    public DetalleVenta(Long id, Long cantidad, BigDecimal descuento, BigDecimal exenta,
                        BigDecimal iva10, BigDecimal iva5, BigDecimal precioVenta, Articulo articulo) {
        this.setId(id); // Heredado de BaseEntity
        this.cantidad = cantidad;
        this.descuento = descuento;
        this.exenta = exenta;
        this.iva10 = iva10;
        this.iva5 = iva5;
        this.precioVenta = precioVenta;
        this.articulo = articulo;
    }

    // Getters y Setters
    public Long getCantidad() { return cantidad; }
    public void setCantidad(Long cantidad) { this.cantidad = cantidad; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getExenta() { return exenta; }
    public void setExenta(BigDecimal exenta) { this.exenta = exenta; }

    public BigDecimal getIva10() { return iva10; }
    public void setIva10(BigDecimal iva10) { this.iva10 = iva10; }

    public BigDecimal getIva5() { return iva5; }
    public void setIva5(BigDecimal iva5) { this.iva5 = iva5; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
}