package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Venta extends BaseEntity {
    private String condicionVenta; // "CON" o "CRE"
    private Date fecha;
    private BigDecimal totalIva5;
    private BigDecimal totalIva10;
    private BigDecimal totalExenta;
    private BigDecimal totalDescuento;
    private BigDecimal totalFactura;
    private Cliente cliente;
    private Usuario usuario;
    private List<DetalleVenta> detallesVenta;

    // Constructores, Getters y Setters
    public Venta() {}

    public Venta(Long id, String condicionVenta, Date fecha, BigDecimal totalIva5, BigDecimal totalIva10,
                 BigDecimal totalExenta, BigDecimal totalDescuento, BigDecimal totalFactura, Cliente cliente,
                 Usuario usuario) {
        this.setId(id);
        this.condicionVenta = condicionVenta;
        this.fecha = fecha;
        this.totalIva5 = totalIva5;
        this.totalIva10 = totalIva10;
        this.totalExenta = totalExenta;
        this.totalDescuento = totalDescuento;
        this.totalFactura = totalFactura;
        this.cliente = cliente;
        this.usuario = usuario;
    }

    public String getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(String condicionVenta) { this.condicionVenta = condicionVenta; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public BigDecimal getTotalIva5() { return totalIva5; }
    public void setTotalIva5(BigDecimal totalIva5) { this.totalIva5 = totalIva5; }

    public BigDecimal getTotalIva10() { return totalIva10; }
    public void setTotalIva10(BigDecimal totalIva10) { this.totalIva10 = totalIva10; }

    public BigDecimal getTotalExenta() { return totalExenta; }
    public void setTotalExenta(BigDecimal totalExenta) { this.totalExenta = totalExenta; }

    public BigDecimal getTotalDescuento() { return totalDescuento; }
    public void setTotalDescuento(BigDecimal totalDescuento) { this.totalDescuento = totalDescuento; }

    public BigDecimal getTotalFactura() { return totalFactura; }
    public void setTotalFactura(BigDecimal totalFactura) { this.totalFactura = totalFactura; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVenta> detallesVenta) { this.detallesVenta = detallesVenta; }
}