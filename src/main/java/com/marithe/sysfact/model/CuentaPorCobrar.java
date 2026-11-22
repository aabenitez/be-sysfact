package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.Date;

/** A credit sale that still has something to pay. */
public class CuentaPorCobrar {

	private Long ventaId;
	private Date fecha;
	/** Date the sale is due: its date plus the credit term (parameter credito.plazo.dias),
	 * as a calendar date (yyyy-MM-dd) so no time zone can shift it. */
	private String vencimiento;
	/** Days past the due date; 0 while not due yet. */
	private long diasAtraso;
	private Cliente cliente;
	private BigDecimal total;
	private BigDecimal cobrado;
	private BigDecimal saldo;
	/** Date of the latest valid payment, or null if nothing was paid yet. */
	private Date ultimoCobro;

	public Long getVentaId() { return ventaId; }
	public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

	public Date getFecha() { return fecha; }
	public void setFecha(Date fecha) { this.fecha = fecha; }

	public String getVencimiento() { return vencimiento; }
	public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }

	public long getDiasAtraso() { return diasAtraso; }
	public void setDiasAtraso(long diasAtraso) { this.diasAtraso = diasAtraso; }

	public Cliente getCliente() { return cliente; }
	public void setCliente(Cliente cliente) { this.cliente = cliente; }

	public BigDecimal getTotal() { return total; }
	public void setTotal(BigDecimal total) { this.total = total; }

	public BigDecimal getCobrado() { return cobrado; }
	public void setCobrado(BigDecimal cobrado) { this.cobrado = cobrado; }

	public BigDecimal getSaldo() { return saldo; }
	public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

	public Date getUltimoCobro() { return ultimoCobro; }
	public void setUltimoCobro(Date ultimoCobro) { this.ultimoCobro = ultimoCobro; }
}
