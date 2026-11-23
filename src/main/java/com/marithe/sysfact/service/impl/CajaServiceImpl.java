package com.marithe.sysfact.service.impl;

import static com.marithe.sysfact.service.impl.VentaServiceImpl.gs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marithe.sysfact.dao.CajaDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.CajaCierre;
import com.marithe.sysfact.model.CajaMovimiento;
import com.marithe.sysfact.model.CajaResumen;
import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.CajaService;

@Service
public class CajaServiceImpl implements CajaService {

	static final int TEXTO_MAX = 250;
	static final int MOTIVO_MIN = 5;
	static final String EFECTIVO = "EFECTIVO";

	@Autowired
	private CajaDao dao;

	@Autowired
	private AuditoriaService auditoria;

	@Autowired
	private UsuarioActual usuarioActual;

	@Override
	public CajaResumen resumen(LocalDate dia) {
		LocalDate d = dia == null ? LocalDate.now() : dia;
		validarDia(d, "ver");
		return armar(d);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CajaResumen cerrar(LocalDate dia, BigDecimal fondoInicial, BigDecimal efectivoContado, String observacion) {
		LocalDate d = dia == null ? LocalDate.now() : dia;
		validarDia(d, "cerrar");
		BigDecimal fondo = monto(fondoInicial == null ? BigDecimal.ZERO : fondoInicial, "El fondo inicial");
		if (efectivoContado == null) {
			throw new BusinessLogicException("Ingresá el efectivo que contaste en la caja.");
		}
		BigDecimal contado = monto(efectivoContado, "El efectivo contado");
		String obs = observacion == null ? "" : observacion.trim();
		if (obs.length() > TEXTO_MAX) {
			throw new BusinessLogicException("La observación puede tener hasta " + TEXTO_MAX + " caracteres.");
		}

		CajaResumen r = armar(d);
		if (r.getCierre() != null) {
			throw new BusinessLogicException("La caja del " + fechaCorta(d) + " ya se cerró. Para cerrarla de nuevo, "
					+ "un Administrador tiene que anular el cierre.");
		}
		BigDecimal esperado = fondo.add(r.getEfectivo());
		BigDecimal diferencia = contado.subtract(esperado);
		if (diferencia.signum() != 0 && obs.length() < MOTIVO_MIN) {
			throw new BusinessLogicException("El efectivo contado no coincide con el esperado ("
					+ textoDiferencia(diferencia) + "): explicá la diferencia en la observación.");
		}

		Usuario u = usuarioActual.get();
		CajaCierre c = new CajaCierre();
		c.setFecha(d.toString());
		c.setFondoInicial(fondo);
		c.setTotalVentas(r.getTotalVentas());
		c.setTotalCobros(r.getTotalCobros());
		c.setEfectivoEsperado(esperado);
		c.setEfectivoContado(contado);
		c.setDiferencia(diferencia);
		c.setObservacion(obs.isEmpty() ? null : obs);
		c.setUsuarioId(u.getId());
		c.setUsuarioNombre(nombre(u));
		dao.insertCierre(c);

		auditoria.registrar(Auditoria.CREAR, Auditoria.CAJA, c.getId(), "Cierre de caja del " + fechaCorta(d)
				+ ": efectivo esperado " + gs(esperado) + ", contado " + gs(contado) + " (" + textoDiferencia(diferencia)
				+ "). Entró " + gs(r.getTotal()) + " en total.", null, c);
		return armar(d);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CajaResumen anular(Long cierreId, String motivo) {
		if (!usuarioActual.esAdministrador()) {
			throw new BusinessLogicException("Solo un Administrador puede anular un cierre de caja.");
		}
		String m = motivo == null ? "" : motivo.trim();
		if (m.length() < MOTIVO_MIN) {
			throw new BusinessLogicException("Escribí el motivo de la anulación (al menos " + MOTIVO_MIN + " caracteres).");
		}
		if (m.length() > TEXTO_MAX) {
			throw new BusinessLogicException("El motivo puede tener hasta " + TEXTO_MAX + " caracteres.");
		}
		CajaCierre antes = dao.getCierre(cierreId);
		if (antes == null) {
			return null;
		}
		if (CajaCierre.ANULADO.equals(antes.getEstado())) {
			throw new BusinessLogicException("Ese cierre de caja ya está anulado.");
		}
		Usuario u = usuarioActual.get();
		if (!dao.anularCierre(cierreId, u.getId(), nombre(u), m)) {
			throw new BusinessLogicException("Ese cierre de caja ya está anulado.");
		}
		LocalDate d = LocalDate.parse(antes.getFecha());
		auditoria.registrar(Auditoria.ANULAR, Auditoria.CAJA, cierreId, "Anuló el cierre de caja del " + fechaCorta(d)
				+ " (contado " + gs(antes.getEfectivoContado()) + "). Motivo: " + m, antes, dao.getCierre(cierreId));
		return armar(d);
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Days that have not come yet have nothing to show. A non-administrator works only with today's
	 * cash, like with sales and payments: older days may already be counted and handed over.
	 */
	private void validarDia(LocalDate d, String accion) {
		LocalDate hoy = LocalDate.now();
		if (d.isAfter(hoy)) {
			throw new BusinessLogicException("No se puede " + accion + " la caja de un día que todavía no llegó.");
		}
		if (!d.equals(hoy) && !usuarioActual.esAdministrador()) {
			throw new BusinessLogicException("Solo un Administrador puede " + accion + " la caja de días anteriores.");
		}
	}

	private CajaResumen armar(LocalDate d) {
		CajaResumen r = new CajaResumen();
		r.setFecha(d.toString());

		Map<String, CajaResumen.PorForma> formas = new LinkedHashMap<>();
		for (String f : Cobro.FORMAS_PAGO) {
			formas.put(f, new CajaResumen.PorForma(f));
		}
		List<CajaMovimiento> movs = new ArrayList<>(dao.ventasContado(d));
		movs.addAll(dao.cobros(d));
		movs.sort(Comparator.comparing(CajaMovimiento::getFecha).thenComparing(CajaMovimiento::getTipo,
				Comparator.reverseOrder()).thenComparing(CajaMovimiento::getId));

		BigDecimal ventas = BigDecimal.ZERO;
		BigDecimal cobros = BigDecimal.ZERO;
		BigDecimal efectivo = BigDecimal.ZERO;
		for (CajaMovimiento m : movs) {
			BigDecimal monto = m.getMonto() == null ? BigDecimal.ZERO : m.getMonto();
			CajaResumen.PorForma pf = formas.computeIfAbsent(m.getFormaPago(), CajaResumen.PorForma::new);
			pf.setCantidad(pf.getCantidad() + 1);
			if (CajaMovimiento.VENTA.equals(m.getTipo())) {
				pf.setVentas(pf.getVentas().add(monto));
				ventas = ventas.add(monto);
			} else {
				pf.setCobros(pf.getCobros().add(monto));
				cobros = cobros.add(monto);
			}
			if (EFECTIVO.equals(m.getFormaPago())) {
				efectivo = efectivo.add(monto);
			}
		}
		r.setMovimientos(movs);
		r.setPorForma(new ArrayList<>(formas.values()));
		r.setTotalVentas(ventas);
		r.setTotalCobros(cobros);
		r.setEfectivo(efectivo);

		for (CajaCierre c : dao.cierres(d)) {
			if (CajaCierre.VIGENTE.equals(c.getEstado())) {
				r.setCierre(c);
			} else {
				r.getCierresAnulados().add(c);
			}
		}
		return r;
	}

	/** Amounts are whole guaraníes, never negative. */
	private static BigDecimal monto(BigDecimal m, String que) {
		if (m.signum() < 0) {
			throw new BusinessLogicException(que + " no puede ser negativo.");
		}
		if (m.stripTrailingZeros().scale() > 0) {
			throw new BusinessLogicException(que + " va en guaraníes, sin decimales.");
		}
		return m.setScale(0);
	}

	static String textoDiferencia(BigDecimal d) {
		if (d.signum() == 0) {
			return "sin diferencia";
		}
		return d.signum() < 0 ? "faltan " + gs(d.negate()) : "sobran " + gs(d);
	}

	private static String nombre(Usuario u) {
		return ((u.getNombres() == null ? "" : u.getNombres().trim()) + " "
				+ (u.getApellidos() == null ? "" : u.getApellidos().trim())).trim();
	}

	private static String fechaCorta(LocalDate f) {
		return String.format("%02d/%02d/%d", f.getDayOfMonth(), f.getMonthValue(), f.getYear());
	}
}
