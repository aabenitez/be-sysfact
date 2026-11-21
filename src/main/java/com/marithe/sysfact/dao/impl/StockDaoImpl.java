package com.marithe.sysfact.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.marithe.sysfact.dao.StockDao;
import com.marithe.sysfact.model.MovimientoStock;
import com.marithe.sysfact.model.Proveedor;

@Repository
public class StockDaoImpl implements StockDao {

	private static final String CREAR_FILA =
			"insert into public.stock (articulo_id, existencia) values (?, 0) on conflict (articulo_id) do nothing";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public long existenciaParaActualizar(Long articuloId) {
		jdbcTemplate.update(CREAR_FILA, articuloId);
		BigDecimal e = jdbcTemplate.queryForObject(
				"select existencia from public.stock where articulo_id = ? for update",
				new Object[] { articuloId }, BigDecimal.class);
		return e == null ? 0 : e.longValue();
	}

	@Override
	public long sumar(Long articuloId, long delta) {
		jdbcTemplate.update(CREAR_FILA, articuloId);
		BigDecimal e = jdbcTemplate.queryForObject(
				"update public.stock set existencia = existencia + ? where articulo_id = ? returning existencia",
				new Object[] { delta, articuloId }, BigDecimal.class);
		return e == null ? 0 : e.longValue();
	}

	@Override
	public void insertMovimiento(MovimientoStock m) {
		Long id = jdbcTemplate.queryForObject(
				"insert into public.movimiento_stock (articulo_id, tipo, cantidad, existencia, usuario_id, usuario_nombre,"
						+ " venta_id, proveedor_id, motivo) values (?, ?, ?, ?, ?, ?, ?, ?, ?) returning id, fecha",
				new Object[] { m.getArticuloId(), m.getTipo(), m.getCantidad(), m.getExistencia(), m.getUsuarioId(),
						m.getUsuarioNombre(), m.getVentaId(), m.getProveedor() == null ? null : m.getProveedor().getId(),
						m.getMotivo() },
				(rs, i) -> {
					// The database sets the date, so the saved movement (and its audit snapshot) carries it.
					m.setFecha(rs.getTimestamp("fecha"));
					return rs.getLong("id");
				});
		m.setId(id);
	}

	@Override
	public List<MovimientoStock> historial(Long articuloId, int limite) {
		return jdbcTemplate.query(
				"select m.*, a.nombre as articulo_nombre, p.nombre as proveedor_nombre from public.movimiento_stock m"
						+ " join public.articulo a on a.id = m.articulo_id"
						+ " left join public.proveedor p on p.id = m.proveedor_id"
						+ " where m.articulo_id = ? order by m.id desc limit ?",
				new Object[] { articuloId, limite }, StockDaoImpl::mapear);
	}

	@Override
	public void updateStockMinimo(Long articuloId, long stockMinimo) {
		jdbcTemplate.update(CREAR_FILA, articuloId);
		jdbcTemplate.update("update public.stock set stock_minimo = ? where articulo_id = ?", stockMinimo, articuloId);
	}

	private static MovimientoStock mapear(ResultSet rs, int fila) throws SQLException {
		MovimientoStock m = new MovimientoStock();
		m.setId(rs.getLong("id"));
		m.setFecha(rs.getTimestamp("fecha"));
		m.setArticuloId(rs.getLong("articulo_id"));
		m.setArticuloNombre(rs.getString("articulo_nombre"));
		m.setTipo(rs.getString("tipo"));
		m.setCantidad(rs.getLong("cantidad"));
		m.setExistencia(rs.getLong("existencia"));
		long usuarioId = rs.getLong("usuario_id");
		m.setUsuarioId(rs.wasNull() ? null : usuarioId);
		m.setUsuarioNombre(rs.getString("usuario_nombre"));
		long ventaId = rs.getLong("venta_id");
		m.setVentaId(rs.wasNull() ? null : ventaId);
		long proveedorId = rs.getLong("proveedor_id");
		if (!rs.wasNull()) {
			Proveedor p = new Proveedor();
			p.setId(proveedorId);
			p.setNombre(rs.getString("proveedor_nombre"));
			m.setProveedor(p);
		}
		m.setMotivo(rs.getString("motivo"));
		return m;
	}
}
