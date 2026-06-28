package com.marithe.sysfact.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.marithe.sysfact.model.Cliente;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteMapper implements RowMapper<Cliente> {

	@Override
	public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
		Cliente cliente = new Cliente();

		cliente.setId(rs.getLong("id"));
		cliente.setApellidos(rs.getString("apellidos"));
		cliente.setNombres(rs.getString("nombres"));
		cliente.setCiRuc(rs.getString("ci_ruc"));
		cliente.setEmail(rs.getString("email"));
		cliente.setTelefono(rs.getString("telefono"));

		return cliente;
	}

}
