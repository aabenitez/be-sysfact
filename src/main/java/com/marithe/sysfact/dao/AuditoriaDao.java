package com.marithe.sysfact.dao;

import java.util.Date;
import java.util.List;

import com.marithe.sysfact.model.Auditoria;

public interface AuditoriaDao {

	/** Adds an entry. The table is append-only: there is no update or delete. */
	void insert(Auditoria a);

	/** Entries of one record, oldest first. */
	List<Auditoria> findByEntidad(String entidad, Long entidadId);

	/**
	 * Entries matching the filters (each one optional), newest first.
	 * @param hasta exclusive upper bound
	 */
	List<Auditoria> buscar(String entidad, String accion, Long usuarioId, Date desde, Date hasta, int limite);
}
