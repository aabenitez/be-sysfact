package com.marithe.sysfact.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.marithe.sysfact.dao.UsuarioDao;
import com.marithe.sysfact.dao.mapper.UsuarioMapper;
import com.marithe.sysfact.dao.sql.UsuarioSql;
import com.marithe.sysfact.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class UsuarioDaoImpl implements UsuarioDao {

    @Autowired
    private UsuarioSql userDaoSql;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private Logger LOGGER = Logger.getLogger(getClass().getName());

    @Override
    public List<Usuario> getAll(Usuario obj) {
        List<Usuario> users = jdbcTemplate.query(userDaoSql.getGetAll(), new UsuarioMapper());
        LOGGER.log(Level.FINE, "getAll users query:", users);
        return users;
    }

    @Override
    public Usuario findById(Long id) {
        // null when it does not exist, so the controller can answer 404.
        List<Usuario> users = jdbcTemplate.query(userDaoSql.getFindById(), new Object[]{id}, new UsuarioMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    /*@Override
    public Usuario findByEmail(String email) {
        Usuario user = jdbcTemplate.queryForObject(userDaoSql.getFindByEmail(), new Object[]{email},
                new UsuarioMapper());
        LOGGER.log(Level.FINE, "findByEmail users query:", user);
        return user;
    }*/

    @Override
    public Usuario findByEmail(String email) {
        try {
            Usuario user = jdbcTemplate.queryForObject(userDaoSql.getFindByEmail(), new Object[]{email},
                    new UsuarioMapper());
            LOGGER.log(Level.FINE, "findByEmail users query found: {0}", email);
            return user;
        } catch (EmptyResultDataAccessException e) {
            LOGGER.log(Level.WARNING, "No se encontro usuario con email: {0}", email);
            return null; // El filtro de seguridad manejará el null correctamente
        }
    }

    @Override
    public Usuario validate(String email, String password) {
        Usuario user = jdbcTemplate.queryForObject(userDaoSql.getValidate(), new Object[]{email, password},
                new UsuarioMapper());
        LOGGER.log(Level.FINE, "validate users query:", user);
        return user;
    }

    @Override
    public Usuario insert(Usuario obj) {
        KeyHolder holder = new GeneratedKeyHolder();
        String sql = userDaoSql.getCreate();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, obj.getNombres());
                ps.setString(2, obj.getApellidos());
                ps.setString(3, obj.getCorreo());
                ps.setLong(4, obj.getRol().getId());
                ps.setString(5, obj.getContrasena());
                return ps;
            }
        }, holder);

        obj.setId((long) holder.getKey().intValue());
        return obj;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(userDaoSql.getDelete(), id);
        LOGGER.log(Level.FINE, "delete users query:");
    }

    @Override
    public Usuario update(Usuario obj) {
        String sql = userDaoSql.getUpdate();
        Object[] params = new Object[]{obj.getApellidos(), obj.getNombres(), obj.getCorreo(), obj.getRol().getId(),
                obj.getId()};
        jdbcTemplate.update(sql, params);
        return obj;
    }

    @Override
    public void changePassoword(String correo, String newPassword) {
        String sql = userDaoSql.getChangePassword();
        Object[] params = new Object[]{newPassword, correo};
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Usuario findByPasswordToken(String token) {
        // Only a token that has not expired matches; null when there is none.
        List<Usuario> users = jdbcTemplate.query(userDaoSql.getFindByPasswordToken(),
                new Object[]{token},
                new UsuarioMapper());
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public List<Usuario> findByCorreoIgnoreCase(String correo) {
        return jdbcTemplate.query(userDaoSql.getGetAll() + " where lower(trim(u.correo)) = lower(trim(?))",
                new Object[]{correo}, new UsuarioMapper());
    }

    @Override
    public void updatePassword(Long id, String hash) {
        jdbcTemplate.update("update usuario set contrasena = ? where id = ?", hash, id);
    }

    @Override
    public long countVentas(Long usuarioId) {
        Long n = jdbcTemplate.queryForObject("select count(*) from venta where usuario_id = ?",
                new Object[]{usuarioId}, Long.class);
        return n == null ? 0 : n;
    }

    @Override
    public long countByRolNombre(String rolNombre) {
        Long n = jdbcTemplate.queryForObject(
                "select count(*) from usuario u join rol r on u.rol_id = r.id where r.nombre = ?",
                new Object[]{rolNombre}, Long.class);
        return n == null ? 0 : n;
    }

    @Override
    public String findRolNombre(Long rolId) {
        List<String> names = jdbcTemplate.queryForList("select nombre from rol where id = ?",
                new Object[]{rolId}, String.class);
        return names.isEmpty() ? null : names.get(0);
    }

    @Override
    public void setPasswordToken(Usuario obj, String token) {
        String sql = userDaoSql.getSetPasswordToken();
        Object[] params = new Object[]{token, obj.getCorreo()};
        jdbcTemplate.update(sql, params);
    }
}
