package com.marithe.sysfact.dao;

import java.time.LocalDate;
import java.util.List;

import com.marithe.sysfact.model.CajaCierre;
import com.marithe.sysfact.model.CajaMovimiento;

public interface CajaDao {

	/** Valid cash sales of the day. */
	List<CajaMovimiento> ventasContado(LocalDate dia);

	/** Valid payments received on the day. */
	List<CajaMovimiento> cobros(LocalDate dia);

	/** Every closing of the day (valid and cancelled), newest first. */
	List<CajaCierre> cierres(LocalDate dia);

	CajaCierre getCierre(Long id);

	/** Saves a closing and fills its id and creation time; fails if the day is already closed. */
	void insertCierre(CajaCierre c);

	/** Cancels a valid closing; false if it was already cancelled. */
	boolean anularCierre(Long id, Long usuarioId, String usuarioNombre, String motivo);
}
