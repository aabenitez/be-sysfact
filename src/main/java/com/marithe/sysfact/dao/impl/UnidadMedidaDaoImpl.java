package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.dao.UnidadMedidaDao;
import com.marithe.sysfact.dao.sql.UnidadMedidaSql;
import com.marithe.sysfact.model.UnidadMedida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class UnidadMedidaDaoImpl implements UnidadMedidaDao {

	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	private UnidadMedidaSql daoSql;
	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	public List<UnidadMedida> getList(UnidadMedida obj) {
		String sql = daoSql.getGetAll();
		List<Object> params = new ArrayList<>();

		if (obj.getNombre() != null) {
			sql += " and nombre like ?";
			params.add("%" + obj.getNombre() + "%");
		}

		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + params.toString());
		// logger.info("parametros del sql: " + params.toString());
		return jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(UnidadMedida.class));
	}

	@Override
	public UnidadMedida getById(Long id) {
		String sql = daoSql.getFindById();
		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + id);
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(UnidadMedida.class));
	}

	@Override
	public UnidadMedida insert(UnidadMedida obj) {
		String sql = daoSql.getInsert();
		Object[] params = new Object[] { obj.getNombre(), obj.getPlural() };
		jdbcTemplate.update(sql, params);
		return obj;
	}

	@Override
	public UnidadMedida update(UnidadMedida obj) {
		String sql = daoSql.getUpdate();
		Object[] params = new Object[] { obj.getNombre(), obj.getPlural(), obj.getId() };
		jdbcTemplate.update(sql, params);
		return obj;
	}

	@Override
	public void delete(Long id) {
		String sql = daoSql.getDelete();
		jdbcTemplate.update(sql, id);
		logger.info("id borrado: " + id);

	}

}
