package com.marithe.sysfact.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marithe.sysfact.model.CajaResumen;
import com.marithe.sysfact.service.CajaService;

/** Daily cash count and closing (Cajero and Administrador). */
@RestController
@RequestMapping("/caja")
public class CajaController {

	@Autowired
	private CajaService service;

	/** Body of POST /caja/cierres. fecha (yyyy-MM-dd) is optional: today by default. */
	public static class CierreRequest {
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		public LocalDate fecha;
		public BigDecimal fondoInicial;
		public BigDecimal efectivoContado;
		public String observacion;
	}

	/** What came in on a day (yyyy-MM-dd, today by default) and its closing. */
	@GetMapping()
	public ResponseEntity<CajaResumen> resumen(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return ResponseEntity.ok(service.resumen(fecha));
	}

	@PostMapping("/cierres")
	public ResponseEntity<CajaResumen> cerrar(@RequestBody CierreRequest body) {
		return new ResponseEntity<>(service.cerrar(body.fecha, body.fondoInicial, body.efectivoContado,
				body.observacion), HttpStatus.CREATED);
	}

	@PostMapping("/cierres/{id}/anular")
	public ResponseEntity<CajaResumen> anular(@PathVariable Long id, @RequestBody Map<String, String> body) {
		CajaResumen r = service.anular(id, body.get("motivo"));
		return r == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(r);
	}
}
