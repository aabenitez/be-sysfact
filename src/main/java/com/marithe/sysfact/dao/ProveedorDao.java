package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ProveedorDao {

    private static final String COLUMNAS = "select id, nombre, contacto, telefono, direccion from public.proveedor";
    private static final String GET_ALL = COLUMNAS + " order by nombre asc";
    private static final String FIND_BY_ID = COLUMNAS + " where id = ?";
    private static final String INSERT =
            "insert into public.proveedor (nombre, contacto, telefono, direccion) values (?, ?, ?, ?)";
    private static final String UPDATE =
            "update public.proveedor set nombre = ?, contacto = ?, telefono = ?, direccion = ? where id = ?";
    private static final String DELETE = "delete from public.proveedor where id = ?";
    private static final String COUNT_ARTICULOS = "select count(*) from public.articulo where proveedor_id = ?";

    private static final RowMapper<Proveedor> MAPPER = (rs, rowNum) -> {
        Proveedor p = new Proveedor();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        return p;
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Proveedor> getAll() {
        return jdbcTemplate.query(GET_ALL, MAPPER);
    }

    /** null when it does not exist. */
    public Proveedor getById(Long id) {
        List<Proveedor> list = jdbcTemplate.query(FIND_BY_ID, new Object[] { id }, MAPPER);
        return list.isEmpty() ? null : list.get(0);
    }

    public Proveedor insert(Proveedor p) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getContacto());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            return ps;
        }, keyHolder);
        p.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        return p;
    }

    public Proveedor update(Proveedor p) {
        jdbcTemplate.update(UPDATE, p.getNombre(), p.getContacto(), p.getTelefono(), p.getDireccion(), p.getId());
        return p;
    }

    public void delete(Long id) {
        jdbcTemplate.update(DELETE, id);
    }

    public long countArticulos(Long proveedorId) {
        Long n = jdbcTemplate.queryForObject(COUNT_ARTICULOS, new Object[] { proveedorId }, Long.class);
        return n == null ? 0 : n;
    }
}
