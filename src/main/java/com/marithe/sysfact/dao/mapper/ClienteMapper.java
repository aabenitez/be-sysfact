package py.com.ventasjdbc.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import py.com.ventasjdbc.model.Cliente;

public class ClienteMapper implements RowMapper<Cliente> {

	@Override
	public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
		Cliente cliente = new Cliente();

		cliente.setId(rs.getLong("id"));
		cliente.setApellidos(rs.getString("apellidos"));
		cliente.setNombres(rs.getString("nombres"));
		cliente.setRucCi(rs.getString("ruc_ci"));
		cliente.setTelefono(rs.getString("telefono"));

		return cliente;
	}

}
