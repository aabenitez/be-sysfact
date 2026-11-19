package com.marithe.sysfact.service;

import java.util.Date;
import java.util.List;

import com.marithe.sysfact.model.Auditoria;

public interface AuditoriaService {

	/**
	 * Records an action of the logged-in user. Must be called inside the transaction of the change, so
	 * the entry is saved if and only if the change is.
	 *
	 * @param antes  the record before the change (null when creating); stored as JSON
	 * @param despues the record after the change (null when deleting); stored as JSON
	 */
	void registrar(String accion, String entidad, Long entidadId, String detalle, Object antes, Object despues);

	List<Auditoria> historial(String entidad, Long entidadId);

	List<Auditoria> buscar(String entidad, String accion, Long usuarioId, Date desde, Date hasta, Integer limite);
}
