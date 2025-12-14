package com.marithe.sysfact.model;

import javax.validation.constraints.NotBlank;

public class Cliente extends BaseEntity {

	@NotBlank(message = "El apellido no puede estar en blanco")
	private String apellidos;
	@NotBlank(message = "El nombre no puede estar en blanco")
	private String nombres;
	@NotBlank(message = "El ruc o cédula no puede estar en blanco")
	private String rucCi;
	@NotBlank(message = "El teléfono no puede estar en blanco")
	private String telefono;

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getRucCi() {
		return rucCi;
	}

	public void setRucCi(String rucCi) {
		this.rucCi = rucCi;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
