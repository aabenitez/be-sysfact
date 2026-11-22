package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A payment received for a credit sale. It is never deleted or changed: a mistake is fixed by
 * cancelling it (estado ANULADO), which keeps who cancelled it, when and why.
 */
public class Cobro extends BaseEntity {

	public static final String VIGENTE = "VIGENTE";
	public static final String ANULADO = "ANULADO";

	public static final List<String> FORMAS_PAGO =
			Arrays.asList("EFECTIVO", "TRANSFERENCIA", "TARJETA", "CHEQUE", "OTRO");

	private Date fecha;
	private Long ventaId;
	private BigDecimal monto;
	/** EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE or OTRO. */
	private String formaPago;
	/** Optional: transfer or cheque number, receipt, etc. */
	private String referencia;
	private Long usuarioId;
	private String usuarioNombre;
	private String estado;
	private Date anulacionFecha;
	private Long anulacionUsuarioId;
	private String anulacionUsuarioNombre;
	private String anulacionMotivo;

	public Date getFecha() { return fecha; }
	public void setFecha(Date fecha) { this.fecha = fecha; }

	public Long getVentaId() { return ventaId; }
	public void setVentaId(Long ventaId) { this.ventaId = ventaId; }

	public BigDecimal getMonto() { return monto; }
	public void setMonto(BigDecimal monto) { this.monto = monto; }

	public String getFormaPago() { return formaPago; }
	public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

	public String getReferencia() { return referencia; }
	public void setReferencia(String referencia) { this.referencia = referencia; }

	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public String getUsuarioNombre() { return usuarioNombre; }
	public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

	public String getEstado() { return estado; }
	public void setEstado(String estado) { this.estado = estado; }

	public Date getAnulacionFecha() { return anulacionFecha; }
	public void setAnulacionFecha(Date anulacionFecha) { this.anulacionFecha = anulacionFecha; }

	public Long getAnulacionUsuarioId() { return anulacionUsuarioId; }
	public void setAnulacionUsuarioId(Long anulacionUsuarioId) { this.anulacionUsuarioId = anulacionUsuarioId; }

	public String getAnulacionUsuarioNombre() { return anulacionUsuarioNombre; }
	public void setAnulacionUsuarioNombre(String anulacionUsuarioNombre) { this.anulacionUsuarioNombre = anulacionUsuarioNombre; }

	public String getAnulacionMotivo() { return anulacionMotivo; }
	public void setAnulacionMotivo(String anulacionMotivo) { this.anulacionMotivo = anulacionMotivo; }
}
