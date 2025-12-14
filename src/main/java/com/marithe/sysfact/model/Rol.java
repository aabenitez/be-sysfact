package py.com.ventasjdbc.model;

import javax.validation.constraints.NotBlank;

public class Rol extends BaseEntity {

	@NotBlank(message = "El nombre no puede estar en blanco")
	private String nombre;
	private String permisos;

	public String getPermisos() {
		return permisos;
	}

	public void setPermisos(String permisos) {
		this.permisos = permisos;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
