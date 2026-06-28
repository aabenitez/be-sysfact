package com.marithe.sysfact.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.marithe.sysfact.dao.RolDao;
import com.marithe.sysfact.dao.sql.RolSql;
import com.marithe.sysfact.model.Rol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class RolDaoImpl implements RolDao {

	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	private RolSql daoSql;
	private Logger logger = Logger.getLogger(getClass().getName());

	@Override
	public List<Rol> getList(Rol obj) {
		String sql = daoSql.getGetAll();
		List<Object> params = new ArrayList<>();

		if (obj.getNombre() != null) {
			sql += " and nombre like ?";
			params.add("%" + obj.getNombre() + "%");
		}

		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + params.toString());
		// logger.info("parametros del sql: " + params.toString());
		return jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(Rol.class));
	}

	@Override
	public Rol getById(Long id) {
		String sql = daoSql.getFindById();
		logger.info("sql a ejecutar: " + sql);
		logger.info("parametros del sql: " + id);
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(Rol.class));
	}

	@Override
	public Rol insert(Rol obj) {
		String sql = daoSql.getInsert();
		Object[] params = new Object[] { obj.getNombre(), obj.getPermisos() };
		jdbcTemplate.update(sql, params);
		return obj;
	}

	@Override
	public Rol update(Rol obj) {
		String sql = daoSql.getUpdate();
		Object[] params = new Object[] { obj.getNombre(), obj.getPermisos(), obj.getId() };
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
