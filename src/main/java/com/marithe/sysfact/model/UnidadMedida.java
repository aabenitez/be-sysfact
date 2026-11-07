package com.marithe.sysfact.model;

import javax.validation.constraints.NotBlank;

public class UnidadMedida extends BaseEntity {

	@NotBlank(message = "El nombre no puede estar en blanco")
	private String nombre;
	private String plural;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPlural() {	return plural; }

	public void setPlural(String plural) { this.plural = plural; }
}
