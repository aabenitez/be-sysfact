package com.marithe.sysfact.model;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class Stock extends BaseEntity {

    @NotNull(message = "El artículo no puede estar nulo")
    private Articulo articulo; // Mapea articulo_id (Es relacional y UNIQUE)

    @NotNull(message = "La existencia no puede estar nula")
    private BigDecimal existencia; // NUMERIC(19,2) en Postgres mapea a BigDecimal

    // Constructores
    public Stock() {}

    public Stock(Long id, Articulo articulo, BigDecimal existencia) {
        this.setId(id); // Heredado de BaseEntity
        this.articulo = articulo;
        this.existencia = existencia;
    }

    // Getters y Setters
    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public BigDecimal getExistencia() { return existencia; }
    public void setExistencia(BigDecimal existencia) { this.existencia = existencia; }
}