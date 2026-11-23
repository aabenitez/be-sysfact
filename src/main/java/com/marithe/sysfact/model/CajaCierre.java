package com.marithe.sysfact.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Closing of one day's cash: the cash counted in the drawer against what should be there (the
 * opening float plus the day's cash sales and cash payments). Never changed or deleted: a wrong
 * one is cancelled by an administrator, with a reason, and the day is closed again.
 */
public class CajaCierre extends BaseEntity {

	public static final String VIGENTE = "VIGENTE";
	public static final String ANULADO = "ANULADO";

	/** Day closed, yyyy-MM-dd. */
	private String fecha;
	private BigDecimal fondoInicial;
	/** Cash sales and payments of the day in every payment method, when it was closed. */
	private BigDecimal totalVentas;
	private BigDecimal totalCobros;
	/** Opening float plus the day's cash (EFECTIVO) sales and payments. */
	private BigDecimal efectivoEsperado;
	private BigDecimal efectivoContado;
	/** Counted minus expected: negative when cash is missing, positive when there is extra. */
	private BigDecimal diferencia;
	private String observacion;
	private Date creado;
	private Long usuarioId;
	private String usuarioNombre;
	private String estado;
	private Date anulacionFecha;
	private Long anulacionUsuarioId;
	private String anulacionUsuarioNombre;
	private String anulacionMotivo;

	public String getFecha() { return fecha; }
	public void setFecha(String fecha) { this.fecha = fecha; }
	public BigDecimal getFondoInicial() { return fondoInicial; }
	public void setFondoInicial(BigDecimal fondoInicial) { this.fondoInicial = fondoInicial; }
	public BigDecimal getTotalVentas() { return totalVentas; }
	public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }
	public BigDecimal getTotalCobros() { return totalCobros; }
	public void setTotalCobros(BigDecimal totalCobros) { this.totalCobros = totalCobros; }
	public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
	public void setEfectivoEsperado(BigDecimal efectivoEsperado) { this.efectivoEsperado = efectivoEsperado; }
	public BigDecimal getEfectivoContado() { return efectivoContado; }
	public void setEfectivoContado(BigDecimal efectivoContado) { this.efectivoContado = efectivoContado; }
	public BigDecimal getDiferencia() { return diferencia; }
	public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
	public String getObservacion() { return observacion; }
	public void setObservacion(String observacion) { this.observacion = observacion; }
	public Date getCreado() { return creado; }
	public void setCreado(Date creado) { this.creado = creado; }
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
