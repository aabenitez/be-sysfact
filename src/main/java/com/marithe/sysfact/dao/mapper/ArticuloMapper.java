package com.marithe.sysfact.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.UnidadMedida;
import com.marithe.sysfact.model.Proveedor;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArticuloMapper implements RowMapper<Articulo> {

    @Override
    public Articulo mapRow(ResultSet rs, int rowNum) throws SQLException {
        Articulo articulo = new Articulo();
        articulo.setId(rs.getLong("id"));
        articulo.setNombre(rs.getString("nombre"));
        articulo.setDescripcion(rs.getString("descripcion"));
        articulo.setPrecio(rs.getBigDecimal("precio"));
        articulo.setNombreImagen(rs.getString("nombre_imagen"));

        // Mapear UnidadMedida
        UnidadMedida um = new UnidadMedida();
        um.setId(rs.getLong("unidad_medida_id"));
        um.setNombre(rs.getString("um_nombre"));
        um.setPlural(rs.getString("um_plural"));
        articulo.setUnidadMedida(um);

        // Mapear Proveedor
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("proveedor_id"));
        p.setNombre(rs.getString("proveedor_nombre"));
        articulo.setProveedor(p);

        return articulo;
    }
}