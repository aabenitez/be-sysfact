package com.marithe.sysfact.dao.impl;

import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.dao.sql.VentaSql;
import com.marithe.sysfact.dao.mapper.VentaMapper;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.model.DetalleVenta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class VentaDaoImpl implements VentaDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate; // Cambiado a protected siguiendo a RolDaoImpl

    @Autowired
    private VentaSql ventaSql;

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public List<Venta> getList(Venta obj) {
        String sql = ventaSql.getGetAll();
        List<Object> params = new ArrayList<>();

        if (obj != null && obj.getCondicionVenta() != null) {
            sql += " and v.condicion_venta = ? ";
            params.add(obj.getCondicionVenta());
        }

        logger.info("sql a ejecutar: " + sql);
        logger.info("parametros del sql: " + params.toString());

        // Usamos el VentaMapper personalizado debido a las relaciones complejas (LEFT/INNER JOIN)
        return jdbcTemplate.query(sql, params.toArray(), new VentaMapper());
    }

    @Override
    public Venta getById(Long id) {
        String sql = ventaSql.getFindById();
        logger.info("sql a ejecutar: " + sql);
        logger.info("parametros del sql: " + id);
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { id }, new VentaMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.warning("No se encontró la venta con el ID: " + id);
            return null; // Evita excepciones si el ID de la venta no existe
        }
    }

    @Override
    public Venta insert(Venta venta) {
        String sqlVenta = ventaSql.getInsert();
        logger.info("sql a ejecutar (insert cabecera): " + sqlVenta);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, venta.getCondicionVenta());
            ps.setBigDecimal(2, venta.getTotalIva5());
            ps.setBigDecimal(3, venta.getTotalIva10());
            ps.setBigDecimal(4, venta.getTotalExenta());
            ps.setBigDecimal(5, venta.getTotalDescuento());
            ps.setBigDecimal(6, venta.getTotalFactura());

            if (venta.getCliente() != null && venta.getCliente().getId() != null) {
                ps.setLong(7, venta.getCliente().getId());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }

            ps.setLong(8, venta.getUsuario().getId());
            return ps;
        }, keyHolder);

        long idVentaGenerado = ((Number) keyHolder.getKeys().get("id")).longValue();
        venta.setId(idVentaGenerado);

        logger.info("Venta insertada exitosamente con ID asignado: " + idVentaGenerado);
        return venta;
    }

    @Override
    public void insertDetalle(DetalleVenta detalle, Long ventaId) {
        String sqlDetalle = ventaSql.getInsertDetalle();

        Object[] params = new Object[] {
                detalle.getCantidad(),
                detalle.getDescuento(),
                detalle.getExenta(),
                detalle.getIva10(),
                detalle.getIva5(),
                detalle.getPrecioVenta(),
                ventaId,
                detalle.getArticulo().getId()
        };

        logger.info("sql a ejecutar (insert detalle): " + sqlDetalle);
        // Descomenta el log de parámetros si deseas ver el arreglo completo en consola
        // logger.info("parametros del detalle: " + java.util.Arrays.toString(params));

        jdbcTemplate.update(sqlDetalle, params);
    }

    @Override
    public List<DetalleVenta> getDetallesByVentaId(Long ventaId) {
        String sql = ventaSql.getGetDetallesByVentaId();
        logger.info("sql a ejecutar (buscar detalles): " + sql);
        logger.info("parametros: " + ventaId);

        return jdbcTemplate.query(sql, new Object[]{ventaId}, (rs, rowNum) -> {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setId(rs.getLong("id"));
            detalle.setCantidad(rs.getLong("cantidad"));
            detalle.setDescuento(rs.getBigDecimal("descuento"));
            detalle.setExenta(rs.getBigDecimal("exenta"));
            detalle.setIva10(rs.getBigDecimal("iva10"));
            detalle.setIva5(rs.getBigDecimal("iva5"));
            detalle.setPrecioVenta(rs.getBigDecimal("precio_venta"));

            // The article as it is now, complete (the audit log keeps a copy of it with each change).
            com.marithe.sysfact.model.Articulo articulo = new com.marithe.sysfact.model.Articulo();
            articulo.setId(rs.getLong("articulo_id"));
            articulo.setNombre(rs.getString("articulo_nombre"));
            articulo.setDescripcion(rs.getString("articulo_descripcion"));
            articulo.setPrecio(rs.getBigDecimal("articulo_precio"));
            articulo.setTasaIva(rs.getInt("articulo_tasa_iva"));
            articulo.setNombreImagen(rs.getString("articulo_nombre_imagen"));
            com.marithe.sysfact.model.UnidadMedida um = new com.marithe.sysfact.model.UnidadMedida();
            um.setId(rs.getLong("unidad_medida_id"));
            um.setNombre(rs.getString("um_nombre"));
            um.setPlural(rs.getString("um_plural"));
            articulo.setUnidadMedida(um);
            com.marithe.sysfact.model.Proveedor proveedor = new com.marithe.sysfact.model.Proveedor();
            proveedor.setId(rs.getLong("proveedor_id"));
            proveedor.setNombre(rs.getString("proveedor_nombre"));
            proveedor.setContacto(rs.getString("proveedor_contacto"));
            proveedor.setTelefono(rs.getString("proveedor_telefono"));
            proveedor.setDireccion(rs.getString("proveedor_direccion"));
            articulo.setProveedor(proveedor);
            detalle.setArticulo(articulo);

            return detalle;
        });
    }

    @Override
    public boolean update(Venta venta) {
        Long clienteId = venta.getCliente() != null ? venta.getCliente().getId() : null;
        return jdbcTemplate.update(ventaSql.getUpdate(), venta.getCondicionVenta(), venta.getTotalIva5(),
                venta.getTotalIva10(), venta.getTotalExenta(), venta.getTotalDescuento(), venta.getTotalFactura(),
                clienteId, venta.getId()) == 1;
    }

    @Override
    public void deleteDetalles(Long ventaId) {
        jdbcTemplate.update(ventaSql.getDeleteDetalles(), ventaId);
    }

    @Override
    public boolean anular(Long ventaId, Long usuarioId, String motivo) {
        return jdbcTemplate.update(ventaSql.getAnular(), usuarioId, motivo, ventaId) == 1;
    }
}