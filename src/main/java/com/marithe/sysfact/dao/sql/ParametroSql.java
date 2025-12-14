package com.marithe.sysfact.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sql/dao/parametro/parametro_sql.properties")
public class ParametroSql {

    @Value("${parametro.getAll}")
    private String getAll;

    @Value("${parametro.findByCodigo}")
    private String findByCodigo;

    public String getGetAll() {
        return getAll;
    }

    public String getFindByCodigo() {
        return findByCodigo;
    }
}
