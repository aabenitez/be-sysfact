package com.marithe.sysfact.service.impl;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marithe.sysfact.dao.AuditoriaDao;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.security.UsuarioActual;
import com.marithe.sysfact.service.AuditoriaService;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

	/** Same entries also go to the server log, under their own logger name. */
	private static final Logger LOG = Logger.getLogger("sysfact.auditoria");

	private static final int LIMITE_POR_DEFECTO = 500;
	private static final int LIMITE_MAXIMO = 2000;
	private static final int MAX_DETALLE = 500;

	@Autowired
	private AuditoriaDao dao;

	@Autowired
	private UsuarioActual usuarioActual;

	private final ObjectMapper json = new ObjectMapper();

	@Override
	// MANDATORY: an audit entry outside the transaction of the change could be saved for a change
	// that is then rolled back, or the other way round.
	@Transactional(propagation = Propagation.MANDATORY)
	public void registrar(String accion, String entidad, Long entidadId, String detalle, Object antes,
			Object despues) {
		Usuario usuario = usuarioActual.get();
		Auditoria a = new Auditoria();
		a.setUsuarioId(usuario.getId());
		a.setUsuarioNombre((usuario.getNombres().trim() + " " + usuario.getApellidos().trim()).trim());
		a.setUsuarioRol(usuarioActual.getRol());
		a.setAccion(accion);
		a.setEntidad(entidad);
		a.setEntidadId(entidadId);
		a.setDetalle(recortar(detalle));
		a.setDatosAntes(aJson(antes));
		a.setDatosDespues(aJson(despues));
		dao.insert(a);

		LOG.info(String.format("%s %s #%s por %s (%s): %s", accion, entidad, entidadId, a.getUsuarioNombre(),
				usuarioActual.getCorreo(), a.getDetalle()));
	}

	@Override
	public List<Auditoria> historial(String entidad, Long entidadId) {
		return dao.findByEntidad(entidad, entidadId);
	}

	@Override
	public List<Auditoria> buscar(String entidad, String accion, Long usuarioId, Date desde, Date hasta,
			Integer limite) {
		int n = limite == null || limite <= 0 ? LIMITE_POR_DEFECTO : Math.min(limite, LIMITE_MAXIMO);
		return dao.buscar(vacioANull(entidad), vacioANull(accion), usuarioId, desde, hasta, n);
	}

	private String aJson(Object obj) {
		if (obj == null) {
			return null;
		}
		try {
			return json.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("No se pudo registrar la auditoría", e);
		}
	}

	private static String recortar(String s) {
		if (s == null) {
			return null;
		}
		return s.length() <= MAX_DETALLE ? s : s.substring(0, MAX_DETALLE - 1) + "…";
	}

	private static String vacioANull(String s) {
		return s == null || s.trim().isEmpty() ? null : s.trim().toUpperCase();
	}
}
