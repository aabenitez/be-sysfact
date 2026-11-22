package com.marithe.sysfact.dao;

import java.math.BigDecimal;
import java.util.List;

import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;

public interface CobroDao {

	/**
	 * Locks the sale row until the transaction ends (so two payments, or a payment and an edit of the
	 * sale, cannot both pass the balance check) and returns the sum of its valid payments.
	 */
	BigDecimal cobradoParaActualizar(Long ventaId);

	/** Sum of the sale's valid payments (0 if none). */
	BigDecimal cobrado(Long ventaId);

	/** All payments of a sale, newest first, cancelled ones included. */
	List<Cobro> porVenta(Long ventaId);

	Cobro getById(Long id);

	/** Inserts the payment and fills in its id and date. */
	void insert(Cobro cobro);

	/** Cancels a valid payment; false if it was already cancelled. */
	boolean anular(Long id, Long usuarioId, String usuarioNombre, String motivo);

	/** Valid credit sales whose payments do not reach the total, oldest first (no due date yet). */
	List<CuentaPorCobrar> pendientes();
}
