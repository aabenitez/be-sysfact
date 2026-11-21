package com.marithe.sysfact.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marithe.sysfact.model.MovimientoStock;
import com.marithe.sysfact.service.ArticuloService;
import com.marithe.sysfact.service.StockService;

/** Stock of an article: its movements, goods received and inventory adjustments. */
@RestController
@RequestMapping("/articulos/{id}/stock")
public class StockController {

	@Autowired
	private StockService service;

	@Autowired
	private ArticuloService articuloService;

	/** Body of POST /ingresos. */
	public static class IngresoRequest {
		public Long cantidad;
		public Long proveedorId;
		public String motivo;
	}

	/** Body of POST /ajustes: the stock that was counted, and why it differs. */
	public static class AjusteRequest {
		public Long existencia;
		public String motivo;
	}

	@GetMapping
	public ResponseEntity<List<MovimientoStock>> historial(@PathVariable Long id) {
		if (articuloService.getById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(service.historial(id));
	}

	@PostMapping("/ingresos")
	public ResponseEntity<MovimientoStock> ingresar(@PathVariable Long id, @RequestBody IngresoRequest body) {
		if (articuloService.getById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		MovimientoStock m = service.ingresar(id, body.cantidad, body.proveedorId, body.motivo);
		return new ResponseEntity<>(m, HttpStatus.CREATED);
	}

	@PostMapping("/ajustes")
	public ResponseEntity<MovimientoStock> ajustar(@PathVariable Long id, @RequestBody AjusteRequest body) {
		if (articuloService.getById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		MovimientoStock m = service.ajustar(id, body.existencia, body.motivo);
		return new ResponseEntity<>(m, HttpStatus.CREATED);
	}
}
