package py.com.ventasjdbc.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import py.com.ventasjdbc.dao.ClienteDao;
import py.com.ventasjdbc.dao.mapper.ClienteMapper;
import py.com.ventasjdbc.dao.sql.ClienteSql;
import py.com.ventasjdbc.model.Cliente;

@Repository
public class ClienteDaoImpl implements ClienteDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ClienteSql daosql;

    @Override
    public List<Cliente> getAll(Cliente obj) {
        String sql = daosql.getGetAll();
        List<Object> params = new ArrayList<>();

        if (obj != null) {
            if (obj.getNombres() != null) {
                sql += " and nombres like upper(?)";
                params.add("%" + obj.getNombres() + "%");
            }

            if (obj.getApellidos() != null) {
                sql += " and apellidos like upper(?)";
                params.add("%" + obj.getApellidos() + "%");
            }

            if (obj.getRucCi() != null) {
                sql += " and ruc_ci=?";
                params.add(obj.getRucCi());
            }
        }

        return jdbcTemplate.query(sql, params.toArray(), new ClienteMapper());
    }

    @Override
    public Cliente findById(Long id) {
        String sql = daosql.getFindById();

        List<Cliente> resultado = jdbcTemplate.query(sql, new Object[]{id}, new ClienteMapper());

        if (resultado.size() > 0) {
            return resultado.get(0);
        } else {
            return null;
        }

    }

    @Override
    public Cliente insert(Cliente obj) {
        KeyHolder nuevoIdGenerado = new GeneratedKeyHolder();
        String sql = daosql.getInsert();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, obj.getApellidos());
                ps.setString(2, obj.getNombres());
                ps.setString(3, obj.getRucCi());
                ps.setObject(4, obj.getTelefono(), Types.VARCHAR); // ojo cuando el campo admite valores nulos, usar
                // setObject
                return ps;
            }
        }, nuevoIdGenerado);
        obj.setId((long) nuevoIdGenerado.getKey().intValue());

        return obj;
    }

    @Override
    public Cliente update(Cliente obj) {
        String sql = daosql.getUpdate();

        List<Object> params = new ArrayList<>();

        if (obj.getApellidos() != null) {
            sql += " apellidos=?, ";
            params.add(obj.getApellidos());
        }

        if (obj.getNombres() != null) {
            sql += " nombres=?, ";
            params.add(obj.getNombres());
        }

        if (obj.getRucCi() != null) {
            sql += " ruc_ci=?, ";
            params.add(obj.getRucCi());
        }

        if (obj.getTelefono() != null) {
            sql += " telefono=?, ";
            params.add(obj.getTelefono());
        }

        sql = sql.substring(0, sql.length() - 2); // quita la última coma antes del where
        sql += " where id=?";
        params.add(obj.getId());

        jdbcTemplate.update(sql, params.toArray());
        return obj;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(daosql.getDelete(), id);
    }

}
