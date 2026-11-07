package com.marithe.sysfact.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:sql/dao/venta/venta_sql.properties")
public class VentaSql {

    @Value("${venta.getAll}")
    private String getAll;

    @Value("${venta.findById}")
    private String findById;

    @Value("${venta.insert}")
    private String insert;

    @Value("${venta.insertDetalle}")
    private String insertDetalle;

    @Value("${venta.updateStock}")
    private String updateStock;

    @Value("${venta.getDetallesByVentaId}")
    private String getDetallesByVentaId;

    // Getters
    public String getGetAll() {
        return getAll;
    }

    public String getFindById() {
        return findById;
    }

    public String getInsert() {
        return insert;
    }

    public String getInsertDetalle() {
        return insertDetalle;
    }

    public String getUpdateStock() {
        return updateStock;
    }

    public String getGetDetallesByVentaId() { return getDetallesByVentaId; }

}