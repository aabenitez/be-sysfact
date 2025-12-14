package com.marithe.sysfact.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.model.Usuario;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioMapper implements RowMapper<Usuario> {

	@Override
	public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
		Usuario user = new Usuario();
		user.setId(rs.getLong("id"));
		user.setNombres(rs.getString("nombres"));
		user.setApellidos(rs.getString("apellidos"));
		user.setCorreo(rs.getString("correo"));
		user.setContrasena(rs.getString("contrasena"));

		Rol rol = new Rol();
		rol.setId(rs.getLong("rol_id"));
		rol.setNombre(rs.getString("rol_nombre"));
		rol.setPermisos(rs.getString("permisos"));

		user.setRol(rol);
		return user;
	}
}
