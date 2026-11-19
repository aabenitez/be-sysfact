package com.marithe.sysfact.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.marithe.sysfact.dao.AuditoriaDao;
import com.marithe.sysfact.model.Auditoria;

@Repository
public class AuditoriaDaoImpl implements AuditoriaDao {

	private static final String COLUMNAS = "select id, fecha, usuario_id, usuario_nombre, usuario_rol, accion, entidad, "
			+ "entidad_id, detalle, datos_antes::text as datos_antes, datos_despues::text as datos_despues "
			+ "from public.auditoria ";

	private static final String INSERT = "insert into public.auditoria (usuario_id, usuario_nombre, usuario_rol, accion, "
			+ "entidad, entidad_id, detalle, datos_antes, datos_despues) values (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void insert(Auditoria a) {
		jdbcTemplate.update(INSERT, a.getUsuarioId(), a.getUsuarioNombre(), a.getUsuarioRol(), a.getAccion(),
				a.getEntidad(), a.getEntidadId(), a.getDetalle(), a.getDatosAntes(), a.getDatosDespues());
	}

	@Override
	public List<Auditoria> findByEntidad(String entidad, Long entidadId) {
		return jdbcTemplate.query(COLUMNAS + "where entidad = ? and entidad_id = ? order by fecha, id",
				new Object[] { entidad, entidadId }, AuditoriaDaoImpl::map);
	}

	@Override
	public List<Auditoria> buscar(String entidad, String accion, Long usuarioId, Date desde, Date hasta, int limite) {
		StringBuilder sql = new StringBuilder(COLUMNAS).append("where 1 = 1 ");
		List<Object> params = new ArrayList<>();
		if (entidad != null) {
			sql.append("and entidad = ? ");
			params.add(entidad);
		}
		if (accion != null) {
			sql.append("and accion = ? ");
			params.add(accion);
		}
		if (usuarioId != null) {
			sql.append("and usuario_id = ? ");
			params.add(usuarioId);
		}
		if (desde != null) {
			sql.append("and fecha >= ? ");
			params.add(new Timestamp(desde.getTime()));
		}
		if (hasta != null) {
			sql.append("and fecha < ? ");
			params.add(new Timestamp(hasta.getTime()));
		}
		sql.append("order by fecha desc, id desc limit ?");
		params.add(limite);
		return jdbcTemplate.query(sql.toString(), params.toArray(), AuditoriaDaoImpl::map);
	}

	private static Auditoria map(ResultSet rs, int rowNum) throws SQLException {
		Auditoria a = new Auditoria();
		a.setId(rs.getLong("id"));
		a.setFecha(rs.getTimestamp("fecha"));
		long usuarioId = rs.getLong("usuario_id");
		a.setUsuarioId(rs.wasNull() ? null : usuarioId);
		a.setUsuarioNombre(rs.getString("usuario_nombre"));
		a.setUsuarioRol(rs.getString("usuario_rol"));
		a.setAccion(rs.getString("accion"));
		a.setEntidad(rs.getString("entidad"));
		long entidadId = rs.getLong("entidad_id");
		a.setEntidadId(rs.wasNull() ? null : entidadId);
		a.setDetalle(rs.getString("detalle"));
		a.setDatosAntes(rs.getString("datos_antes"));
		a.setDatosDespues(rs.getString("datos_despues"));
		return a;
	}
}
