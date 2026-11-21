package com.marithe.sysfact.model;

import java.util.Date;

/**
 * One change to an article's stock. cantidad is the change (+ in, - out) and existencia the stock
 * right after it, so the list of movements explains every number.
 */
public class MovimientoStock extends BaseEntity {

	/** Stock the article had when the history started (written by the migration). */
	public static final String INICIAL = "INICIAL";
	/** Goods received. */
	public static final String INGRESO = "INGRESO";
	/** Correction after a count, breakage, loss… */
	public static final String AJUSTE = "AJUSTE";
	/** Sold (or more units after editing a sale). */
	public static final String VENTA = "VENTA";
	/** Back to stock: a cancelled sale, or fewer units after editing one. */
	public static final String DEVOLUCION = "DEVOLUCION";

	private Date fecha;
	private Long articuloId;
	private String articuloNombre;
	private String tipo;
	private Long cantidad;
	private Long existencia;
	private Long usuarioId;
	private String usuarioNombre;
	private Long ventaId;
	private Proveedor proveedor;
	private String motivo;

	public Date getFecha() { return fecha; }
	public void setFecha(Date fecha) { this.fecha = fecha; }

	public Long getArticuloId() { return articuloId; }
	public void setArticuloId(Long articuloId) { this.articuloId = articuloId; }

	public String getArticuloNombre() { return articuloNombre; }
	public void setArticuloNombre(String articuloNombre) { this.articuloNombre = articuloNombre; }

	public String getTipo() { return tipo; }
	public void setTipo(String tipo) { this.tipo = tipo; }

	public Long getCantidad() { return cantidad; }
	public void setCantidad(Long cantidad) { this.cantidad = cantidad; }

	public Long getExistencia() { return existencia; }
	public void setExistencia(Long existencia) { this.existencia = existencia; }

	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public String getUsuarioNombre() { return usuarioNombre; }
	public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

	public Long getVentaId() { return ventaId; }
	public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

	public Proveedor getProveedor() { return proveedor; }
	public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

	public String getMotivo() { return motivo; }
	public void setMotivo(String motivo) { this.motivo = motivo; }
}
