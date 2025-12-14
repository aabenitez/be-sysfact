package com.marithe.sysfact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.marithe.sysfact.constraints.NestedIdConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class Usuario extends BaseEntity {

	@NotBlank(message = "El nombre no puede estar en blanco")
	private String nombres;
	@NotBlank(message = "El apellido no puede estar en blanco")
	private String apellidos;
	@NotBlank(message = "El correo no puede estar en blanco")
	@Email(message = "Formato inválido para correo electrónico")
	private String correo;
	@NestedIdConstraint(message = "El id del rol no puede estar nulo")
	private Rol rol;
	@JsonProperty(access = Access.WRITE_ONLY)
	@NotBlank(message = "La contraseña no puede estar en blanco")
	private String contrasena;

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

}
