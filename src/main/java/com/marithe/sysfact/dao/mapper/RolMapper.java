package com.marithe.sysfact.dao.mapper;

import com.marithe.sysfact.model.Rol;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RolMapper implements RowMapper<Rol> {

    @Override
    public Rol mapRow(ResultSet rs, int rowNum) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getLong("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setPermisos(rs.getString("permisos"));
        return rol;
    }
}
