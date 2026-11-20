package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.dao.UnidadMedidaDao;
import com.marithe.sysfact.dao.sql.UnidadMedidaSql;
import com.marithe.sysfact.model.UnidadMedida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
		sql += " order by nombre";

		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + params.toString());
		return jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(UnidadMedida.class));
	}

	@Override
	public UnidadMedida getById(Long id) {
		String sql = daoSql.getFindById();
		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + id);
		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(UnidadMedida.class));
		} catch (EmptyResultDataAccessException e) {
			// No row: the controller answers 404 instead of 500.
			return null;
		}
	}

	@Override
	public List<UnidadMedida> findByNombre(String nombre) {
		// Case-insensitive, so "maceta" and "Maceta" count as the same unit.
		return jdbcTemplate.query("select * from unidad_medida where lower(nombre) = lower(?)",
				new Object[] { nombre }, new BeanPropertyRowMapper<>(UnidadMedida.class));
	}

	@Override
	public long countArticulos(Long id) {
		Long n = jdbcTemplate.queryForObject("select count(*) from articulo where unidad_medida_id = ?",
				new Object[] { id }, Long.class);
		return n == null ? 0 : n;
	}

	@Override
	public UnidadMedida insert(UnidadMedida obj) {
		String sql = daoSql.getInsert();
		logger.info("sql a ejecutar: " + sql);
		Long id = jdbcTemplate.queryForObject(sql, new Object[] { obj.getNombre(), obj.getPlural() }, Long.class);
		obj.setId(id);
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
