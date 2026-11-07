package com.marithe.sysfact.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.model.Usuario;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentaMapper implements RowMapper<Venta> {

    @Override
    public Venta mapRow(ResultSet rs, int rowNum) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getLong("id"));
        venta.setCondicionVenta(rs.getString("condicion_venta"));
        venta.setFecha(rs.getTimestamp("fecha"));
        venta.setTotalIva5(rs.getBigDecimal("total_iva5"));
        venta.setTotalIva10(rs.getBigDecimal("total_iva10"));
        venta.setTotalExenta(rs.getBigDecimal("total_exenta"));
        venta.setTotalDescuento(rs.getBigDecimal("total_descuento"));
        venta.setTotalFactura(rs.getBigDecimal("total_factura"));

        // Mapeamos el Usuario
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("usuario_id"));
        usuario.setNombres(rs.getString("usuario_nombres"));
        usuario.setApellidos(rs.getString("usuario_apellidos"));
        usuario.setCorreo(rs.getString("usuario_correo"));
        venta.setUsuario(usuario);

        // Mapeamos el Cliente
        long clienteId = rs.getLong("cliente_id");
        if (!rs.wasNull()) {
            Cliente cliente = new Cliente();
            cliente.setId(clienteId);
            cliente.setCiRuc(rs.getString("cliente_ci_ruc"));
            cliente.setNombres(rs.getString("cliente_nombres"));
            cliente.setApellidos(rs.getString("cliente_apellidos"));
            cliente.setEmail(rs.getString("cliente_email"));
            cliente.setTelefono(rs.getString("cliente_telefono"));
            venta.setCliente(cliente);
        }

        return venta;
    }
}