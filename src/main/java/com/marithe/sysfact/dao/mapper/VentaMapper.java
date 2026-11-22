package com.marithe.sysfact.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.model.Rol;
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
        venta.setEstado(rs.getString("estado"));
        venta.setCobrado(rs.getBigDecimal("cobrado"));
        venta.setAnulacionFecha(rs.getTimestamp("anulacion_fecha"));
        venta.setAnulacionMotivo(rs.getString("anulacion_motivo"));
        long anuladoPor = rs.getLong("anulacion_usuario_id");
        if (!rs.wasNull()) {
            Usuario anulador = new Usuario();
            anulador.setId(anuladoPor);
            anulador.setNombres(rs.getString("anulacion_usuario_nombres"));
            anulador.setApellidos(rs.getString("anulacion_usuario_apellidos"));
            anulador.setCorreo(rs.getString("anulacion_usuario_correo"));
            anulador.setRol(rol(rs, "anulacion_rol_"));
            venta.setAnulacionUsuario(anulador);
        }

        // Mapeamos el Usuario
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("usuario_id"));
        usuario.setNombres(rs.getString("usuario_nombres"));
        usuario.setApellidos(rs.getString("usuario_apellidos"));
        usuario.setCorreo(rs.getString("usuario_correo"));
        usuario.setRol(rol(rs, "usuario_rol_"));
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

    /** Role of a user (columns {prefix}id, {prefix}nombre, {prefix}permisos), or null without one. */
    private static Rol rol(ResultSet rs, String prefijo) throws SQLException {
        long id = rs.getLong(prefijo + "id");
        if (rs.wasNull()) {
            return null;
        }
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(rs.getString(prefijo + "nombre"));
        rol.setPermisos(rs.getString(prefijo + "permisos"));
        return rol;
    }
}