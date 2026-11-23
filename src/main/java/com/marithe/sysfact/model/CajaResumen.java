package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** What came in on one day, by payment method, and the day's closing if it was closed. */
public class CajaResumen {

	/** Totals of one payment method. */
	public static class PorForma {
		private String formaPago;
		private BigDecimal ventas = BigDecimal.ZERO;
		private BigDecimal cobros = BigDecimal.ZERO;
		private int cantidad;

		public PorForma() {}
		public PorForma(String formaPago) { this.formaPago = formaPago; }

		public String getFormaPago() { return formaPago; }
		public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
		public BigDecimal getVentas() { return ventas; }
		public void setVentas(BigDecimal ventas) { this.ventas = ventas; }
		public BigDecimal getCobros() { return cobros; }
		public void setCobros(BigDecimal cobros) { this.cobros = cobros; }
		/** Number of movements. */
		public int getCantidad() { return cantidad; }
		public void setCantidad(int cantidad) { this.cantidad = cantidad; }
		public BigDecimal getTotal() { return ventas.add(cobros); }
	}

	/** Day, yyyy-MM-dd. */
	private String fecha;
	/** Cash sales and valid payments of the day, oldest first. Cancelled ones are left out. */
	private List<CajaMovimiento> movimientos = new ArrayList<>();
	/** One entry per payment method (all of them, in a fixed order, even when zero). */
	private List<PorForma> porForma = new ArrayList<>();
	private BigDecimal totalVentas = BigDecimal.ZERO;
	private BigDecimal totalCobros = BigDecimal.ZERO;
	/** Cash (EFECTIVO) that came in, without the opening float. */
	private BigDecimal efectivo = BigDecimal.ZERO;
	/** The day's valid closing, or null while it is open. */
	private CajaCierre cierre;
	/** Closings of the day that were cancelled, newest first. */
	private List<CajaCierre> cierresAnulados = new ArrayList<>();

	public String getFecha() { return fecha; }
	public void setFecha(String fecha) { this.fecha = fecha; }
	public List<CajaMovimiento> getMovimientos() { return movimientos; }
	public void setMovimientos(List<CajaMovimiento> movimientos) { this.movimientos = movimientos; }
	public List<PorForma> getPorForma() { return porForma; }
	public void setPorForma(List<PorForma> porForma) { this.porForma = porForma; }
	public BigDecimal getTotalVentas() { return totalVentas; }
	public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }
	public BigDecimal getTotalCobros() { return totalCobros; }
	public void setTotalCobros(BigDecimal totalCobros) { this.totalCobros = totalCobros; }
	public BigDecimal getTotal() { return totalVentas.add(totalCobros); }
	public BigDecimal getEfectivo() { return efectivo; }
	public void setEfectivo(BigDecimal efectivo) { this.efectivo = efectivo; }
	public CajaCierre getCierre() { return cierre; }
	public void setCierre(CajaCierre cierre) { this.cierre = cierre; }
	public List<CajaCierre> getCierresAnulados() { return cierresAnulados; }
	public void setCierresAnulados(List<CajaCierre> cierresAnulados) { this.cierresAnulados = cierresAnulados; }
}
