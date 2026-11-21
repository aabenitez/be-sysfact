package com.marithe.sysfact.service;

import java.util.List;
import java.util.Map;

import com.marithe.sysfact.model.MovimientoStock;

public interface StockService {

	/** Goods received: adds cantidad (> 0). proveedorId and motivo are optional. */
	MovimientoStock ingresar(Long articuloId, Long cantidad, Long proveedorId, String motivo);

	/** Inventory correction: sets the stock to existencia (what was counted). motivo is required. */
	MovimientoStock ajustar(Long articuloId, Long existencia, String motivo);

	/**
	 * Stock changes caused by a sale: one movement per article with a non-zero change (negative when
	 * units go out). Runs inside the sale's transaction.
	 */
	void moverPorVenta(Long ventaId, Map<Long, Long> cambios, String motivo);

	/** Latest movements of the article, newest first. */
	List<MovimientoStock> historial(Long articuloId);
}
