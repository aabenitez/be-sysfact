package com.marithe.sysfact.dao;

import java.util.List;

import com.marithe.sysfact.model.MovimientoStock;

public interface StockDao {

	/**
	 * Current stock of the article, locking its row until the transaction ends so two changes at the
	 * same time cannot both start from the same number. Creates the row (with 0) if it is missing.
	 */
	long existenciaParaActualizar(Long articuloId);

	/** Adds delta (negative to subtract) and returns the new stock. */
	long sumar(Long articuloId, long delta);

	void insertMovimiento(MovimientoStock m);

	/** Movements of the article, newest first. */
	List<MovimientoStock> historial(Long articuloId, int limite);

	/** Sets the minimum stock (creating the row if it is missing). */
	void updateStockMinimo(Long articuloId, long stockMinimo);
}
