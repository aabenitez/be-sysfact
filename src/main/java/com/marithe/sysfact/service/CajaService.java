package com.marithe.sysfact.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.marithe.sysfact.model.CajaResumen;

/** Daily cash count: what came in by payment method, and closing the day against the counted cash. */
public interface CajaService {

	/** The day's movements and totals (today when dia is null). */
	CajaResumen resumen(LocalDate dia);

	/** Closes the day (today when dia is null) with the counted cash; returns the updated summary. */
	CajaResumen cerrar(LocalDate dia, BigDecimal fondoInicial, BigDecimal efectivoContado, String observacion);

	/** Cancels a closing (administrators only), or null if it does not exist. */
	CajaResumen anular(Long cierreId, String motivo);
}
