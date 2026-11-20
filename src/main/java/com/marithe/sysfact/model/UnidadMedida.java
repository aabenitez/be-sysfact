package com.marithe.sysfact.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UnidadMedida extends BaseEntity {

	@NotBlank(message = "El nombre no puede estar en blanco")
	@Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
	private String nombre;

	@NotBlank(message = "El plural no puede estar en blanco")
	@Size(max = 100, message = "El plural puede tener hasta 100 caracteres")
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
