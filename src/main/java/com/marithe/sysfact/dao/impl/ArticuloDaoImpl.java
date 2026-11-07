package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.sql.ArticuloSql;
import com.marithe.sysfact.dao.mapper.ArticuloMapper;
import com.marithe.sysfact.model.Articulo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class ArticuloDaoImpl implements ArticuloDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private ArticuloSql daoSql;

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public List<Articulo> getList(Articulo obj) {
        String sql = daoSql.getGetAll();
        List<Object> params = new ArrayList<>();

        if (obj != null && obj.getNombre() != null) {
            sql += " and a.nombre like ? ";
            params.add("%" + obj.getNombre() + "%");
        }

        logger.info("sql a ejecutar: " + sql);
        logger.info("parametros del sql: " + params.toString());
        return jdbcTemplate.query(sql, params.toArray(), new ArticuloMapper());
    }

    @Override
    public Articulo getById(Long id) {
        String sql = daoSql.getFindById();
        logger.info("sql a ejecutar: " + sql);
        logger.info("parametros del sql: " + id);
        return jdbcTemplate.queryForObject(sql, new Object[] { id }, new ArticuloMapper());
    }

    @Override
    public Articulo insert(Articulo obj) {
        String sql = daoSql.getInsert();
        logger.info("sql a ejecutar (insert articulo): " + sql);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, obj.getNombre());
            ps.setString(2, obj.getDescripcion());
            ps.setBigDecimal(3, obj.getPrecio());
            ps.setLong(4, obj.getUnidadMedida().getId());
            ps.setLong(5, obj.getProveedor().getId());
            ps.setString(6, obj.getNombreImagen());
            return ps;
        }, keyHolder);

        long idGenerado = ((Number) keyHolder.getKeys().get("id")).longValue();
        obj.setId(idGenerado);
        return obj;
    }

    @Override
    public Articulo update(Articulo obj) {
        String sql = daoSql.getUpdate();
        Object[] params = new Object[] {
                obj.getNombre(),
                obj.getDescripcion(),
                obj.getPrecio(),
                obj.getUnidadMedida().getId(),
                obj.getProveedor().getId(),
                obj.getNombreImagen(),
                obj.getId()
        };
        logger.info("sql a ejecutar (update articulo): " + sql);
        jdbcTemplate.update(sql, params);
        return obj;
    }

    @Override
    public void delete(Long id) {
        String sql = daoSql.getDelete();
        logger.info("sql a ejecutar (delete articulo): " + sql);
        jdbcTemplate.update(sql, id);
    }
}