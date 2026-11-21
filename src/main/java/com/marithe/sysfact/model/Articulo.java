package com.marithe.sysfact.model;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Articulo extends BaseEntity {

    @NotBlank(message = "El nombre del artículo no puede estar en blanco")
    @Size(max = 100, message = "El nombre del artículo no puede superar los 100 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Debe especificar el precio del artículo")
    @DecimalMin(value = "0", message = "El precio no puede ser negativo")
    private BigDecimal precio;

    /**
     * Tasa de IVA del artículo: 10, 5 o 0 (exenta). Si no se envía al crear, se usa el parámetro
     * impuesto.iva.general.
     */
    private Integer tasaIva;

    @NotNull(message = "Debe especificar una unidad de medida")
    private UnidadMedida unidadMedida; // Mapea unidad_medida_id

    @NotNull(message = "Debe especificar un proveedor")
    private Proveedor proveedor; // Mapea proveedor_id

    private String nombreImagen;

    /** Units in stock now (read-only here: it changes through goods received, adjustments and sales). */
    private Long existencia;

    /** At or below this stock the article shows as low ("poco stock"). 0 = only when it runs out. */
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Max(value = 1000000, message = "El stock mínimo puede ser de hasta 1.000.000")
    private Long stockMinimo;

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

    public Integer getTasaIva() { return tasaIva; }
    public void setTasaIva(Integer tasaIva) { this.tasaIva = tasaIva; }

    public String getNombreImagen() { return nombreImagen; }
    public void setNombreImagen(String nombreImagen) { this.nombreImagen = nombreImagen; }

    public Long getExistencia() { return existencia; }
    public void setExistencia(Long existencia) { this.existencia = existencia; }

    public Long getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Long stockMinimo) { this.stockMinimo = stockMinimo; }
}