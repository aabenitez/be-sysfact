package com.marithe.sysfact.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * One entry of the audit log: who did what to which record, and the record before and after.
 * datosAntes / datosDespues hold JSON and are sent to the client as JSON objects, not strings.
 */
public class Auditoria extends BaseEntity {

	public static final String CREAR = "CREAR";
	public static final String EDITAR = "EDITAR";
	public static final String ANULAR = "ANULAR";
	public static final String ELIMINAR = "ELIMINAR";

	public static final String VENTA = "VENTA";
	public static final String CLIENTE = "CLIENTE";

	private Date fecha;
	private Long usuarioId;
	private String usuarioNombre;
	private String usuarioRol;
	private String accion;
	private String entidad;
	private Long entidadId;
	private String detalle;
	@JsonRawValue
	private String datosAntes;
	@JsonRawValue
	private String datosDespues;

	public Date getFecha() { return fecha; }
	public void setFecha(Date fecha) { this.fecha = fecha; }

	public Long getUsuarioId() { return usuarioId; }
	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public String getUsuarioNombre() { return usuarioNombre; }
	public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

	public String getUsuarioRol() { return usuarioRol; }
	public void setUsuarioRol(String usuarioRol) { this.usuarioRol = usuarioRol; }

	public String getAccion() { return accion; }
	public void setAccion(String accion) { this.accion = accion; }

	public String getEntidad() { return entidad; }
	public void setEntidad(String entidad) { this.entidad = entidad; }

	public Long getEntidadId() { return entidadId; }
	public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

	public String getDetalle() { return detalle; }
	public void setDetalle(String detalle) { this.detalle = detalle; }

	public String getDatosAntes() { return datosAntes; }
	public void setDatosAntes(String datosAntes) { this.datosAntes = datosAntes; }

	public String getDatosDespues() { return datosDespues; }
	public void setDatosDespues(String datosDespues) { this.datosDespues = datosDespues; }
}
