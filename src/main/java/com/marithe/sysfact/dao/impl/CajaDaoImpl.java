package com.marithe.sysfact.dao.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.marithe.sysfact.dao.CajaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.CajaCierre;
import com.marithe.sysfact.model.CajaMovimiento;

@Repository
public class CajaDaoImpl implements CajaDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<CajaMovimiento> ventasContado(LocalDate dia) {
		return jdbcTemplate.query("select v.id, v.fecha, v.total_factura, v.forma_pago,"
				+ " c.nombres, c.apellidos, u.nombres as u_nombres, u.apellidos as u_apellidos"
				+ " from public.venta v"
				+ " join public.usuario u on u.id = v.usuario_id"
				+ " left join public.cliente c on c.id = v.cliente_id"
				+ " where v.condicion_venta = 'CON' and v.estado = 'EMITIDA' and v.fecha >= ? and v.fecha < ?"
				+ " order by v.fecha, v.id", new Object[] { inicio(dia), inicio(dia.plusDays(1)) }, (rs, i) -> {
					CajaMovimiento m = new CajaMovimiento();
					m.setTipo(CajaMovimiento.VENTA);
					m.setId(rs.getLong("id"));
					m.setVentaId(rs.getLong("id"));
					m.setFecha(rs.getTimestamp("fecha"));
					m.setMonto(rs.getBigDecimal("total_factura"));
					m.setFormaPago(rs.getString("forma_pago") == null ? "EFECTIVO" : rs.getString("forma_pago"));
					m.setCliente(nombre(rs.getString("nombres"), rs.getString("apellidos")));
					m.setUsuarioNombre(nombre(rs.getString("u_nombres"), rs.getString("u_apellidos")));
					return m;
				});
	}

	@Override
	public List<CajaMovimiento> cobros(LocalDate dia) {
		return jdbcTemplate.query("select co.id, co.fecha, co.venta_id, co.monto, co.forma_pago, co.referencia,"
				+ " co.usuario_nombre, c.nombres, c.apellidos"
				+ " from public.cobro co"
				+ " join public.venta v on v.id = co.venta_id"
				+ " left join public.cliente c on c.id = v.cliente_id"
				+ " where co.estado = 'VIGENTE' and co.fecha >= ? and co.fecha < ?"
				+ " order by co.fecha, co.id", new Object[] { inicio(dia), inicio(dia.plusDays(1)) }, (rs, i) -> {
					CajaMovimiento m = new CajaMovimiento();
					m.setTipo(CajaMovimiento.COBRO);
					m.setId(rs.getLong("id"));
					m.setVentaId(rs.getLong("venta_id"));
					m.setFecha(rs.getTimestamp("fecha"));
					m.setMonto(rs.getBigDecimal("monto"));
					m.setFormaPago(rs.getString("forma_pago"));
					m.setReferencia(rs.getString("referencia"));
					m.setCliente(nombre(rs.getString("nombres"), rs.getString("apellidos")));
					m.setUsuarioNombre(rs.getString("usuario_nombre"));
					return m;
				});
	}

	@Override
	public List<CajaCierre> cierres(LocalDate dia) {
		return jdbcTemplate.query("select * from public.caja_cierre where fecha = ? order by id desc",
				new Object[] { Date.valueOf(dia) }, (rs, i) -> mapear(rs));
	}

	@Override
	public CajaCierre getCierre(Long id) {
		List<CajaCierre> l = jdbcTemplate.query("select * from public.caja_cierre where id = ?", new Object[] { id },
				(rs, i) -> mapear(rs));
		return l.isEmpty() ? null : l.get(0);
	}

	@Override
	public void insertCierre(CajaCierre c) {
		try {
			Long id = jdbcTemplate.queryForObject("insert into public.caja_cierre (fecha, fondo_inicial, total_ventas,"
					+ " total_cobros, efectivo_esperado, efectivo_contado, diferencia, observacion, usuario_id,"
					+ " usuario_nombre) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id, creado",
					new Object[] { Date.valueOf(c.getFecha()), c.getFondoInicial(), c.getTotalVentas(),
							c.getTotalCobros(), c.getEfectivoEsperado(), c.getEfectivoContado(), c.getDiferencia(),
							c.getObservacion(), c.getUsuarioId(), c.getUsuarioNombre() },
					(rs, i) -> {
						c.setCreado(rs.getTimestamp("creado"));
						return rs.getLong("id");
					});
			c.setId(id);
			c.setEstado(CajaCierre.VIGENTE);
		} catch (DuplicateKeyException e) {
			// Someone else closed the same day a moment earlier (one valid closing per day).
			throw new BusinessLogicException("La caja de ese día ya se cerró. Actualizá la página para ver el cierre.");
		}
	}

	@Override
	public boolean anularCierre(Long id, Long usuarioId, String usuarioNombre, String motivo) {
		return jdbcTemplate.update("update public.caja_cierre set estado = 'ANULADO', anulacion_fecha = current_timestamp,"
				+ " anulacion_usuario_id = ?, anulacion_usuario_nombre = ?, anulacion_motivo = ?"
				+ " where id = ? and estado = 'VIGENTE'", usuarioId, usuarioNombre, motivo, id) == 1;
	}

	private static Timestamp inicio(LocalDate dia) {
		return Timestamp.valueOf(dia.atStartOfDay());
	}

	private static String nombre(String nombres, String apellidos) {
		String n = ((nombres == null ? "" : nombres.trim()) + " " + (apellidos == null ? "" : apellidos.trim())).trim();
		return n.isEmpty() ? null : n;
	}

	private static CajaCierre mapear(ResultSet rs) throws SQLException {
		CajaCierre c = new CajaCierre();
		c.setId(rs.getLong("id"));
		c.setFecha(rs.getDate("fecha").toLocalDate().toString());
		c.setFondoInicial(rs.getBigDecimal("fondo_inicial"));
		c.setTotalVentas(rs.getBigDecimal("total_ventas"));
		c.setTotalCobros(rs.getBigDecimal("total_cobros"));
		c.setEfectivoEsperado(rs.getBigDecimal("efectivo_esperado"));
		c.setEfectivoContado(rs.getBigDecimal("efectivo_contado"));
		c.setDiferencia(rs.getBigDecimal("diferencia"));
		c.setObservacion(rs.getString("observacion"));
		c.setCreado(rs.getTimestamp("creado"));
		long u = rs.getLong("usuario_id");
		c.setUsuarioId(rs.wasNull() ? null : u);
		c.setUsuarioNombre(rs.getString("usuario_nombre"));
		c.setEstado(rs.getString("estado"));
		c.setAnulacionFecha(rs.getTimestamp("anulacion_fecha"));
		long au = rs.getLong("anulacion_usuario_id");
		c.setAnulacionUsuarioId(rs.wasNull() ? null : au);
		c.setAnulacionUsuarioNombre(rs.getString("anulacion_usuario_nombre"));
		c.setAnulacionMotivo(rs.getString("anulacion_motivo"));
		return c;
	}
}
