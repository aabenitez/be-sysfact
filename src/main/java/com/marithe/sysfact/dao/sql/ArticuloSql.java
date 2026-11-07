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

    public String getGetAll() { return getAll; }
    public String getFindById() { return findById; }
    public String getInsert() { return insert; }
    public String getUpdate() { return update; }
    public String getDelete() { return delete; }
}