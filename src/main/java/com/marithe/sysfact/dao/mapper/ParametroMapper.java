package py.com.ventasjdbc.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import py.com.ventasjdbc.model.Parametro;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametroMapper implements RowMapper<Parametro> {

    @Override
    public Parametro mapRow(ResultSet rs, int rowNum) throws SQLException {
        Parametro parametro = new Parametro();
        parametro.setId(rs.getLong("id"));
        parametro.setDescripcion(rs.getString("descripcion"));
        parametro.setCodigo(rs.getString("codigo"));
        parametro.setActivo(rs.getBoolean("activo"));
        parametro.setValor(rs.getString("valor"));
        return parametro;
    }
}
