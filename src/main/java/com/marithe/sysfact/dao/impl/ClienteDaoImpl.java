package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.dao.mapper.ClienteMapper;
import com.marithe.sysfact.dao.sql.ClienteSql;
import com.marithe.sysfact.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClienteDaoImpl implements ClienteDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ClienteSql daosql;

    //@Override
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

            if (obj.getCiRuc() != null) {
                sql += " and ci_ruc=?";
                params.add(obj.getCiRuc());
            }

            if (obj.getEmail() != null) {
                sql += " email=?, ";
                params.add(obj.getEmail());
            }

            if (obj.getTelefono() != null) {
                sql += " telefono=?, ";
                params.add(obj.getTelefono());
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
                ps.setString(3, obj.getCiRuc());
                ps.setString(4, obj.getEmail());
                ps.setObject(5, obj.getTelefono(), Types.VARCHAR); // ojo cuando el campo admite valores nulos, usar
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
        Object[] params = new Object[] {obj.getNombres(), obj.getApellidos(), obj.getCiRuc(),
                obj.getEmail(), obj.getTelefono(), obj.getId()};
        jdbcTemplate.update(sql, params);
        return obj;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(daosql.getDelete(), id);
    }

}
