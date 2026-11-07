package com.marithe.sysfact.dao.mapper;

import com.marithe.sysfact.model.UnidadMedida;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UnidadMedidaMapper implements RowMapper<UnidadMedida> {

    @Override
    public UnidadMedida mapRow(ResultSet rs, int rowNum) throws SQLException {
        UnidadMedida rol = new UnidadMedida();
        rol.setId(rs.getLong("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setPlural(rs.getString("plural"));
        return rol;
    }
}
