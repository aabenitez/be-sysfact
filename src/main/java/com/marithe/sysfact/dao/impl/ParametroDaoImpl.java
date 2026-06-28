package com.marithe.sysfact.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.marithe.sysfact.dao.ParametroDao;
import com.marithe.sysfact.dao.mapper.ParametroMapper;
import com.marithe.sysfact.dao.sql.ParametroSql;
import com.marithe.sysfact.model.Parametro;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class ParametroDaoImpl implements ParametroDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private ParametroSql daoSql;

    private Logger logger = Logger.getLogger(getClass().getName());

    public List<Parametro> getAll() {
        String sql = daoSql.getGetAll();
        logger.info("sql a ejecutar: " + sql);
        return jdbcTemplate.query(sql, new ArrayList<>().toArray(), new ParametroMapper());
    }

    public Parametro findByCodigo(String codigo) {
        String sql = daoSql.getFindByCodigo();
        return jdbcTemplate.queryForObject(sql, new Object[]{codigo}, new ParametroMapper());
    }

    public void insert(Parametro obj) {
        String sql = "INSERT INTO parametros (descripcion, codigo, activo, valor) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, obj.getDescripcion(), obj.getCodigo(), obj.getActivo(), obj.getValor());
    }

    public void update(Parametro obj) {
        String sql = "UPDATE parametros SET descripcion=?, codigo=?, activo=?, valor=? WHERE id=?";
        jdbcTemplate.update(sql, obj.getDescripcion(), obj.getCodigo(), obj.getActivo(), obj.getValor(), obj.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM parametros WHERE id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Parametro findById(Long id) {
        String sql = daoSql.getFindById();
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new ParametroMapper());
    }
}
