package com.marithe.sysfact.service.impl;

import static com.marithe.sysfact.service.impl.VentaServiceImpl.gs;
import static com.marithe.sysfact.service.impl.VentaServiceImpl.numero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marithe.sysfact.dao.CobroDao;
import com.marithe.sysfact.dao.ParametroDao;
import com.marithe.sysfact.dao.VentaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.model.Venta;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.CobroService;

@Service
public class CobroServiceImpl implements CobroService {

	static final String PARAMETRO_PLAZO = "credito.plazo.dias";
	static final int PLAZO_DEFECTO = 30;
	static final int REFERENCIA_MAX = 100;
	static final int MOTIVO_MIN = 5;
	static final int MOTIVO_MAX = 250;

	@Autowired
	private CobroDao dao;

	@Autowired
	private VentaDao ventaDao;

	@Autowired
	private ParametroDao parametroDao;

	@Autowired
	private AuditoriaService auditoria;

	@Autowired
	private UsuarioActual usuarioActual;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Cobro registrar(Long ventaId, BigDecimal monto, String formaPago, String referencia) {
		if (monto == null || monto.signum() <= 0) {
			throw new BusinessLogicException("El monto cobrado tiene que ser mayor que cero.");
		}
		if (monto.stripTrailingZeros().scale() > 0) {
			throw new BusinessLogicException("El monto va en guaraníes, sin decimales.");
		}
		if (formaPago == null || !Cobro.FORMAS_PAGO.contains(formaPago)) {
			throw new BusinessLogicException("Elegí la forma de pago.");
		}
		String ref = referencia == null || referencia.trim().isEmpty() ? null : referencia.trim();
		if (ref != null && ref.length() > REFERENCIA_MAX) {
			throw new BusinessLogicException("La referencia puede tener hasta " + REFERENCIA_MAX + " caracteres.");
		}

		Venta venta = ventaDao.getById(ventaId);
		if (venta == null) {
			return null;
		}
		if (Venta.ANULADA.equals(venta.getEstado())) {
			throw new BusinessLogicException("La factura N.º " + numero(ventaId) + " está anulada: no se puede cobrar.");
		}
		if (!"CRE".equals(venta.getCondicionVenta())) {
			throw new BusinessLogicException("La factura N.º " + numero(ventaId)
					+ " es al contado: se cobró al emitirla.");
		}

		BigDecimal cobrado = dao.cobradoParaActualizar(ventaId);
		BigDecimal saldo = total(venta).subtract(cobrado);
		if (saldo.signum() <= 0) {
			throw new BusinessLogicException("La factura N.º " + numero(ventaId) + " ya está pagada.");
		}
		if (monto.compareTo(saldo) > 0) {
			throw new BusinessLogicException("El saldo de la factura N.º " + numero(ventaId) + " es " + gs(saldo)
					+ ": no se puede cobrar " + gs(monto) + ".");
		}

		Usuario u = usuarioActual.get();
		Cobro c = new Cobro();
		c.setVentaId(ventaId);
		c.setMonto(monto);
		c.setFormaPago(formaPago);
		c.setReferencia(ref);
		c.setUsuarioId(u.getId());
		c.setUsuarioNombre(nombre(u));
		dao.insert(c);

		BigDecimal nuevoSaldo = saldo.subtract(monto);
		auditoria.registrar(Auditoria.CREAR, Auditoria.COBRO, c.getId(),
				"Factura N.º " + numero(ventaId) + ": cobró " + gs(monto) + " " + forma(formaPago)
						+ (ref == null ? "" : " (" + ref + ")") + ". Saldo " + gs(saldo) + " → " + gs(nuevoSaldo),
				null, foto(c, venta, nuevoSaldo));
		return c;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Cobro anular(Long ventaId, Long cobroId, String motivo) {
		String m = motivo == null ? "" : motivo.trim();
		if (m.length() < MOTIVO_MIN) {
			throw new BusinessLogicException("Escribí el motivo de la anulación (al menos " + MOTIVO_MIN + " caracteres).");
		}
		if (m.length() > MOTIVO_MAX) {
			throw new BusinessLogicException("El motivo puede tener hasta " + MOTIVO_MAX + " caracteres.");
		}
		Cobro c = dao.getById(cobroId);
		if (c == null || !c.getVentaId().equals(ventaId)) {
			return null;
		}
		if (Cobro.ANULADO.equals(c.getEstado())) {
			throw new BusinessLogicException("Ese cobro ya está anulado.");
		}
		// Like sales: payments of earlier days may already be in a cash count.
		if (!usuarioActual.esAdministrador() && !esDeHoy(c.getFecha())) {
			throw new BusinessLogicException("Solo un Administrador puede anular cobros de días anteriores.");
		}

		Venta venta = ventaDao.getById(ventaId);
		BigDecimal saldoAntes = total(venta).subtract(dao.cobradoParaActualizar(ventaId));
		Usuario u = usuarioActual.get();
		if (!dao.anular(cobroId, u.getId(), nombre(u), m)) {
			throw new BusinessLogicException("Ese cobro ya está anulado.");
		}
		Cobro despues = dao.getById(cobroId);
		BigDecimal saldoDespues = saldoAntes.add(c.getMonto());
		auditoria.registrar(Auditoria.ANULAR, Auditoria.COBRO, cobroId,
				"Factura N.º " + numero(ventaId) + ": anuló el cobro de " + gs(c.getMonto()) + " del "
						+ fechaCorta(c.getFecha()) + ". Saldo " + gs(saldoAntes) + " → " + gs(saldoDespues)
						+ ". Motivo: " + m,
				foto(c, venta, saldoAntes), foto(despues, venta, saldoDespues));
		return despues;
	}

	@Override
	public List<Cobro> porVenta(Long ventaId) {
		return dao.porVenta(ventaId);
	}

	@Override
	public List<CuentaPorCobrar> pendientes() {
		int plazo = plazoDias();
		LocalDate hoy = LocalDate.now();
		List<CuentaPorCobrar> l = dao.pendientes();
		for (CuentaPorCobrar p : l) {
			LocalDate vence = local(p.getFecha()).plusDays(plazo);
			p.setVencimiento(vence.toString());
			p.setDiasAtraso(Math.max(0, ChronoUnit.DAYS.between(vence, hoy)));
		}
		return l;
	}

	@Override
	public int plazoDias() {
		Parametro p = parametroDao.findByCodigo(PARAMETRO_PLAZO);
		if (p == null || p.getValor() == null) {
			return PLAZO_DEFECTO;
		}
		try {
			int d = Integer.parseInt(p.getValor().trim());
			return d >= 0 ? d : PLAZO_DEFECTO;
		} catch (NumberFormatException e) {
			return PLAZO_DEFECTO;
		}
	}

	// ---------------------------------------------------------------------------------------------

	private static BigDecimal total(Venta v) {
		return v.getTotalFactura() == null ? BigDecimal.ZERO : v.getTotalFactura();
	}

	/** Snapshot for the audit log: the payment, its sale and the balance after it. */
	private static Map<String, Object> foto(Cobro c, Venta v, BigDecimal saldo) {
		Map<String, Object> f = new LinkedHashMap<>();
		f.put("cobro", c);
		f.put("ventaId", v.getId());
		f.put("totalFactura", v.getTotalFactura());
		f.put("saldo", saldo);
		if (v.getCliente() != null) {
			f.put("clienteId", v.getCliente().getId());
		}
		return f;
	}

	static String forma(String formaPago) {
		switch (formaPago) {
		case "EFECTIVO": return "en efectivo";
		case "TRANSFERENCIA": return "por transferencia";
		case "TARJETA": return "con tarjeta";
		case "CHEQUE": return "con cheque";
		default: return "(otra forma de pago)";
		}
	}

	private static String nombre(Usuario u) {
		return ((u.getNombres() == null ? "" : u.getNombres().trim()) + " "
				+ (u.getApellidos() == null ? "" : u.getApellidos().trim())).trim();
	}

	private static LocalDate local(Date d) {
		return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private static boolean esDeHoy(Date d) {
		return d != null && local(d).equals(LocalDate.now());
	}

	private static String fechaCorta(Date d) {
		LocalDate f = local(d);
		return String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
	}
}
