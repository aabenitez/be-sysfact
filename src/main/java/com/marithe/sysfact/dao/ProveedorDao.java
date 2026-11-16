package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProveedorDao {

    private static final String GET_ALL =
            "select id, nombre, contacto, telefono, direccion from public.proveedor order by nombre asc";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Proveedor> getAll() {
        return jdbcTemplate.query(GET_ALL, (rs, rowNum) -> {
            Proveedor p = new Proveedor();
            p.setId(rs.getLong("id"));
            p.setNombre(rs.getString("nombre"));
            p.setContacto(rs.getString("contacto"));
            p.setTelefono(rs.getString("telefono"));
            p.setDireccion(rs.getString("direccion"));
            return p;
        });
    }
}
