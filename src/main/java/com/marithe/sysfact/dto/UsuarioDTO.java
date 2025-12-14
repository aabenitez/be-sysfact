package com.marithe.sysfact.dto;

import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.model.Usuario;

public class UsuarioDTO {
	private Long id;
	private String nombres;
	private String apellidos;
	private String correo;
	private Rol rol;
	private String token;

	public UsuarioDTO(Usuario user, String token) {
		this.id = user.getId();
		this.nombres = user.getNombres();
		this.apellidos = user.getApellidos();
		this.correo = user.getCorreo();
		this.rol = user.getRol();
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}