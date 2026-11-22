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
    /** EMITIDA or ANULADA. A cancelled sale is kept, with who cancelled it, when and why. */
    private String estado;
    private Date anulacionFecha;
    private Usuario anulacionUsuario;
    private String anulacionMotivo;
    /** Sum of the valid payments (credit sales). */
    private BigDecimal cobrado;
    /** Credit sales: date it is due (its date plus the credit term). Null for cash sales. */
    /** Due date of a credit sale as a calendar date (yyyy-MM-dd), so no time zone can shift it. */
    private String vencimiento;

    public static final String EMITIDA = "EMITIDA";
    public static final String ANULADA = "ANULADA";

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getAnulacionFecha() { return anulacionFecha; }
    public void setAnulacionFecha(Date anulacionFecha) { this.anulacionFecha = anulacionFecha; }

    public Usuario getAnulacionUsuario() { return anulacionUsuario; }
    public void setAnulacionUsuario(Usuario anulacionUsuario) { this.anulacionUsuario = anulacionUsuario; }

    public String getAnulacionMotivo() { return anulacionMotivo; }
    public void setAnulacionMotivo(String anulacionMotivo) { this.anulacionMotivo = anulacionMotivo; }
    public BigDecimal getCobrado() { return cobrado == null ? BigDecimal.ZERO : cobrado; }
    public void setCobrado(BigDecimal cobrado) { this.cobrado = cobrado; }
    public String getVencimiento() { return vencimiento; }
    public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }
    /** What is still owed: only a valid credit sale owes anything (cash sales are paid when issued). */
    public BigDecimal getSaldo() {
        if (!"CRE".equals(condicionVenta) || ANULADA.equals(estado) || totalFactura == null) {
            return BigDecimal.ZERO;
        }
        return totalFactura.subtract(getCobrado()).max(BigDecimal.ZERO);
    }
}