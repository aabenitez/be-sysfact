package com.marithe.sysfact.model;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Articulo extends BaseEntity {

    @NotBlank(message = "El nombre del artículo no puede estar en blanco")
    private String nombre;

    private String descripcion;

    private BigDecimal precio;

    @NotNull(message = "Debe especificar una unidad de medida")
    private UnidadMedida unidadMedida; // Mapea unidad_medida_id

    @NotNull(message = "Debe especificar un proveedor")
    private Proveedor proveedor; // Mapea proveedor_id

    private String nombreImagen;

    // Constructores
    public Articulo() {}

    public Articulo(Long id, String nombre, String descripcion, BigDecimal precio,
                    UnidadMedida unidadMedida, Proveedor proveedor, String nombreImagen) {
        this.setId(id); // Heredado de BaseEntity
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
        this.proveedor = proveedor;
        this.nombreImagen = nombreImagen;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getNombreImagen() { return nombreImagen; }
    public void setNombreImagen(String nombreImagen) { this.nombreImagen = nombreImagen; }
}