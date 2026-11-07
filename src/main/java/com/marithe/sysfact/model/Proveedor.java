package com.marithe.sysfact.model;

import javax.validation.constraints.NotBlank;

public class Proveedor extends BaseEntity {

    @NotBlank(message = "El nombre del proveedor no puede estar en blanco")
    private String nombre;

    private String contacto;

    private String telefono;

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

    public String getDirection() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}