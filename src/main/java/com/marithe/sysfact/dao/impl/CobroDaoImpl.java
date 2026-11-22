package com.marithe.sysfact.dao.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.marithe.sysfact.dao.CobroDao;
import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;

@Repository
public class CobroDaoImpl implements CobroDao {

	private static final String SUMA_VIGENTES =
			"select coalesce(sum(monto), 0) from public.cobro where venta_id = ? and estado = 'VIGENTE'";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public BigDecimal cobradoParaActualizar(Long ventaId) {
		jdbcTemplate.query("select id from public.venta where id = ? for update", rs -> {}, ventaId);
		return cobrado(ventaId);
	}

	@Override
	public BigDecimal cobrado(Long ventaId) {
		BigDecimal s = jdbcTemplate.queryForObject(SUMA_VIGENTES, new Object[] { ventaId }, BigDecimal.class);
		return s == null ? BigDecimal.ZERO : s;
	}

	@Override
	public List<Cobro> porVenta(Long ventaId) {
		return jdbcTemplate.query("select * from public.cobro where venta_id = ? order by id desc",
				new Object[] { ventaId }, (rs, i) -> mapear(rs));
	}

	@Override
	public Cobro getById(Long id) {
		List<Cobro> l = jdbcTemplate.query("select * from public.cobro where id = ?", new Object[] { id },
				(rs, i) -> mapear(rs));
		return l.isEmpty() ? null : l.get(0);
	}

	@Override
	public void insert(Cobro c) {
		Long id = jdbcTemplate.queryForObject(
				"insert into public.cobro (venta_id, monto, forma_pago, referencia, usuario_id, usuario_nombre)"
						+ " values (?, ?, ?, ?, ?, ?) returning id, fecha",
				new Object[] { c.getVentaId(), c.getMonto(), c.getFormaPago(), c.getReferencia(), c.getUsuarioId(),
						c.getUsuarioNombre() },
				(rs, i) -> {
					c.setFecha(rs.getTimestamp("fecha"));
					return rs.getLong("id");
				});
		c.setId(id);
		c.setEstado(Cobro.VIGENTE);
	}

	@Override
	public boolean anular(Long id, Long usuarioId, String usuarioNombre, String motivo) {
		return jdbcTemplate.update("update public.cobro set estado = 'ANULADO', anulacion_fecha = current_timestamp,"
				+ " anulacion_usuario_id = ?, anulacion_usuario_nombre = ?, anulacion_motivo = ?"
				+ " where id = ? and estado = 'VIGENTE'", usuarioId, usuarioNombre, motivo, id) == 1;
	}

	@Override
	public List<CuentaPorCobrar> pendientes() {
		return jdbcTemplate.query("select v.id, v.fecha, v.total_factura,"
				+ " coalesce(sum(co.monto) filter (where co.estado = 'VIGENTE'), 0) as cobrado,"
				+ " max(co.fecha) filter (where co.estado = 'VIGENTE') as ultimo_cobro,"
				+ " c.id as cliente_id, c.nombres, c.apellidos, c.ci_ruc, c.telefono, c.email"
				+ " from public.venta v"
				+ " left join public.cliente c on c.id = v.cliente_id"
				+ " left join public.cobro co on co.venta_id = v.id"
				+ " where v.condicion_venta = 'CRE' and v.estado = 'EMITIDA'"
				+ " group by v.id, c.id"
				+ " having v.total_factura > coalesce(sum(co.monto) filter (where co.estado = 'VIGENTE'), 0)"
				+ " order by v.fecha, v.id", (rs, i) -> {
					CuentaPorCobrar p = new CuentaPorCobrar();
					p.setVentaId(rs.getLong("id"));
					p.setFecha(rs.getTimestamp("fecha"));
					p.setTotal(rs.getBigDecimal("total_factura"));
					p.setCobrado(rs.getBigDecimal("cobrado"));
					p.setSaldo(p.getTotal().subtract(p.getCobrado()));
					p.setUltimoCobro(rs.getTimestamp("ultimo_cobro"));
					long clienteId = rs.getLong("cliente_id");
					if (!rs.wasNull()) {
						Cliente c = new Cliente();
						c.setId(clienteId);
						c.setNombres(rs.getString("nombres"));
						c.setApellidos(rs.getString("apellidos"));
						c.setCiRuc(rs.getString("ci_ruc"));
						c.setTelefono(rs.getString("telefono"));
						c.setEmail(rs.getString("email"));
						p.setCliente(c);
					}
					return p;
				});
	}

	private static Cobro mapear(ResultSet rs) throws SQLException {
		Cobro c = new Cobro();
		c.setId(rs.getLong("id"));
		c.setFecha(rs.getTimestamp("fecha"));
		c.setVentaId(rs.getLong("venta_id"));
		c.setMonto(rs.getBigDecimal("monto"));
		c.setFormaPago(rs.getString("forma_pago"));
		c.setReferencia(rs.getString("referencia"));
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
