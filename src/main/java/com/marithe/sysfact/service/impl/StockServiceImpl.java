package com.marithe.sysfact.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.dao.StockDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.MovimientoStock;
import com.marithe.sysfact.model.Proveedor;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;
import com.marithe.sysfact.service.StockService;

@Service
public class StockServiceImpl implements StockService {

	static final long CANTIDAD_MAX = 1_000_000L;
	static final int MOTIVO_MIN = 5;
	static final int MOTIVO_MAX = 250;
	static final int HISTORIAL_LIMITE = 200;

	@Autowired
	private StockDao dao;

	@Autowired
	private ArticuloDao articuloDao;

	@Autowired
	private AuditoriaService auditoria;

	@Autowired
	private UsuarioActual usuarioActual;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MovimientoStock ingresar(Long articuloId, Long cantidad, Long proveedorId, String motivo) {
		Articulo articulo = articulo(articuloId);
		if (cantidad == null || cantidad <= 0) {
			throw new BusinessLogicException("La cantidad que ingresa tiene que ser mayor que cero.");
		}
		if (cantidad > CANTIDAD_MAX) {
			throw new BusinessLogicException("La cantidad puede ser de hasta " + CANTIDAD_MAX + ".");
		}
		String m = textoOpcional(motivo);

		long antes = dao.existenciaParaActualizar(articuloId);
		long despues = dao.sumar(articuloId, cantidad);
		MovimientoStock mov = nuevo(articuloId, MovimientoStock.INGRESO, cantidad, despues, m);
		if (proveedorId != null) {
			Proveedor p = new Proveedor();
			p.setId(proveedorId);
			mov.setProveedor(p);
		}
		dao.insertMovimiento(mov);

		auditoria.registrar(Auditoria.INGRESO, Auditoria.STOCK, articuloId,
				nombre(articulo) + ": +" + cantidad + " (" + antes + " → " + despues + ")" + (m == null ? "" : ". " + m),
				foto(articulo, antes), foto(articulo, despues, mov));
		return mov;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MovimientoStock ajustar(Long articuloId, Long existencia, String motivo) {
		Articulo articulo = articulo(articuloId);
		if (existencia == null || existencia < 0) {
			throw new BusinessLogicException("La existencia contada no puede ser negativa.");
		}
		if (existencia > CANTIDAD_MAX) {
			throw new BusinessLogicException("La existencia puede ser de hasta " + CANTIDAD_MAX + ".");
		}
		String m = motivo == null ? "" : motivo.trim();
		if (m.length() < MOTIVO_MIN) {
			throw new BusinessLogicException("Escribí el motivo del ajuste (al menos " + MOTIVO_MIN + " caracteres).");
		}
		if (m.length() > MOTIVO_MAX) {
			throw new BusinessLogicException("El motivo puede tener hasta " + MOTIVO_MAX + " caracteres.");
		}

		long antes = dao.existenciaParaActualizar(articuloId);
		long delta = existencia - antes;
		if (delta == 0) {
			throw new BusinessLogicException("La existencia de \"" + nombre(articulo) + "\" ya es " + antes
					+ ": no hay nada que ajustar.");
		}
		long despues = dao.sumar(articuloId, delta);
		MovimientoStock mov = nuevo(articuloId, MovimientoStock.AJUSTE, delta, despues, m);
		dao.insertMovimiento(mov);

		auditoria.registrar(Auditoria.AJUSTE, Auditoria.STOCK, articuloId,
				nombre(articulo) + ": " + antes + " → " + despues + " (" + (delta > 0 ? "+" : "") + delta + "). Motivo: " + m,
				foto(articulo, antes), foto(articulo, despues, mov));
		return mov;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void moverPorVenta(Long ventaId, Map<Long, Long> cambios, String motivo) {
		Usuario usuario = usuarioActual.get();
		for (Map.Entry<Long, Long> e : cambios.entrySet()) {
			long delta = e.getValue() == null ? 0 : e.getValue();
			if (delta == 0) {
				continue;
			}
			long despues = dao.sumar(e.getKey(), delta);
			MovimientoStock mov = nuevo(e.getKey(), delta < 0 ? MovimientoStock.VENTA : MovimientoStock.DEVOLUCION,
					delta, despues, motivo, usuario);
			mov.setVentaId(ventaId);
			dao.insertMovimiento(mov);
		}
	}

	@Override
	public List<MovimientoStock> historial(Long articuloId) {
		return dao.historial(articuloId, HISTORIAL_LIMITE);
	}

	// ---------------------------------------------------------------------------------------------

	private Articulo articulo(Long id) {
		Articulo a = id == null ? null : articuloDao.getById(id);
		if (a == null) {
			throw new BusinessLogicException("El artículo " + id + " no existe.");
		}
		return a;
	}

	private MovimientoStock nuevo(Long articuloId, String tipo, long cantidad, long existencia, String motivo) {
		return nuevo(articuloId, tipo, cantidad, existencia, motivo, usuarioActual.get());
	}

	private static MovimientoStock nuevo(Long articuloId, String tipo, long cantidad, long existencia, String motivo,
			Usuario usuario) {
		MovimientoStock m = new MovimientoStock();
		m.setArticuloId(articuloId);
		m.setTipo(tipo);
		m.setCantidad(cantidad);
		m.setExistencia(existencia);
		m.setUsuarioId(usuario.getId());
		m.setUsuarioNombre((usuario.getNombres().trim() + " " + usuario.getApellidos().trim()).trim());
		m.setMotivo(motivo);
		return m;
	}

	/** Snapshot for the audit log: the article and its stock at that moment. */
	private static Map<String, Object> foto(Articulo a, long existencia) {
		return foto(a, existencia, null);
	}

	private static Map<String, Object> foto(Articulo a, long existencia, MovimientoStock mov) {
		Map<String, Object> f = new LinkedHashMap<>();
		f.put("articuloId", a.getId());
		f.put("articulo", nombre(a));
		f.put("existencia", existencia);
		if (mov != null) {
			f.put("movimiento", mov);
		}
		return f;
	}

	private static String nombre(Articulo a) {
		return a.getNombre() == null ? "artículo " + a.getId() : a.getNombre().trim();
	}

	private static String textoOpcional(String s) {
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		String t = s.trim();
		if (t.length() > MOTIVO_MAX) {
			throw new BusinessLogicException("La nota puede tener hasta " + MOTIVO_MAX + " caracteres.");
		}
		return t;
	}
}
