package com.marithe.sysfact.service;

import java.math.BigDecimal;
import java.util.List;

import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;

public interface CobroService {

	/**
	 * Registers a payment for a valid credit sale; it cannot exceed the balance. referencia is
	 * optional. Returns null if the sale does not exist.
	 */
	Cobro registrar(Long ventaId, BigDecimal monto, String formaPago, String referencia);

	/** Cancels a payment of the sale, with a reason. Returns null if it does not exist. */
	Cobro anular(Long ventaId, Long cobroId, String motivo);

	/** Payments of the sale, newest first, cancelled ones included. */
	List<Cobro> porVenta(Long ventaId);

	/** Credit sales with a balance, oldest first, with their due date and days past due. */
	List<CuentaPorCobrar> pendientes();

	/** Credit term in days (parameter credito.plazo.dias, 30 if missing or invalid). */
	int plazoDias();
}
