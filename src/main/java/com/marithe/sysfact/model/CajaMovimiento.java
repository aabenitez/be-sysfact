package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.Date;

/** Money that came in on a day: a cash sale or a payment of a credit sale. */
public class CajaMovimiento {

	public static final String VENTA = "VENTA";
	public static final String COBRO = "COBRO";

	/** VENTA (cash sale) or COBRO (payment of a credit sale). */
	private String tipo;
	/** Id of the sale (VENTA) or of the payment (COBRO). */
	private Long id;
	private Long ventaId;
	private Date fecha;
	private String cliente;
	private BigDecimal monto;
	private String formaPago;
	private String referencia;
	private String usuarioNombre;

	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getVentaId() { return ventaId; }
	public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
	public Date getFecha() { return fecha; }
	public void setFecha(Date fecha) { this.fecha = fecha; }
	public String getCliente() { return cliente; }
	public void setCliente(String cliente) { this.cliente = cliente; }
	public BigDecimal getMonto() { return monto; }
	public void setMonto(BigDecimal monto) { this.monto = monto; }
	public String getFormaPago() { return formaPago; }
	public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
	public String getReferencia() { return referencia; }
	public void setReferencia(String referencia) { this.referencia = referencia; }
	public String getUsuarioNombre() { return usuarioNombre; }
	public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
}
