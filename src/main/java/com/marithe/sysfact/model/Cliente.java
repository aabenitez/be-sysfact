package com.marithe.sysfact.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Cliente extends BaseEntity {

	@NotBlank(message = "El apellido no puede estar en blanco")
	@Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
	private String apellidos;
	@NotBlank(message = "El nombre no puede estar en blanco")
	@Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
	private String nombres;
	@NotBlank(message = "El Cédula o Ruc no puede estar en blanco")
	@Size(max = 20, message = "La cédula o RUC no puede superar los 20 caracteres")
	private String ciRuc;
	@Email(message = "El email no tiene un formato válido")
	@Size(max = 100, message = "El email no puede superar los 100 caracteres")
	private String email;
	@NotBlank(message = "El teléfono no puede estar en blanco")
	@Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
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

	public String getCiRuc() {
		return ciRuc;
	}

	public void setCiRuc(String ciRuc) { this.ciRuc = ciRuc; }

	public String getEmail() { return email; }

	public void setEmail(String email) { this.email = email; }

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
