package com.marithe.sysfact.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:sql/dao/articulo/articulo_sql.properties")
public class ArticuloSql {

    @Value("${articulo.getAll}")
    private String getAll;

    @Value("${articulo.findById}")
    private String findById;

    @Value("${articulo.insert}")
    private String insert;

    @Value("${articulo.update}")
    private String update;

    @Value("${articulo.delete}")
    private String delete;

    @Value("${articulo.insertStock}")
    private String insertStock;

    @Value("${articulo.deleteStock}")
    private String deleteStock;

    @Value("${articulo.countVentas}")
    private String countVentas;

    public String getGetAll() { return getAll; }
    public String getFindById() { return findById; }
    public String getInsert() { return insert; }
    public String getUpdate() { return update; }
    public String getDelete() { return delete; }
    public String getInsertStock() { return insertStock; }
    public String getDeleteStock() { return deleteStock; }
    public String getCountVentas() { return countVentas; }
}