package com.marithe.sysfact.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Proveedor extends BaseEntity {

    @NotBlank(message = "El nombre del proveedor no puede estar en blanco")
    @Size(max = 100, message = "El nombre del proveedor no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El contacto no puede superar los 100 caracteres")
    private String contacto;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @Size(max = 100, message = "La dirección no puede superar los 100 caracteres")
    private String direccion;

    // Constructor vacío estándar
    public Proveedor() {}

    // Constructor completo
    public Proveedor(Long id, String nombre, String contacto, String telefono, String direccion) {
        this.setId(id);
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
