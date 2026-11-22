package com.marithe.sysfact.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.marithe.sysfact.model.Cobro;
import com.marithe.sysfact.model.CuentaPorCobrar;
import com.marithe.sysfact.service.CobroService;
import com.marithe.sysfact.service.VentaService;

/** Accounts receivable: payments of credit sales and the list of what is still owed. */
@RestController
public class CobroController {

	@Autowired
	private CobroService service;

	@Autowired
	private VentaService ventaService;

	/** Body of POST /ventas/{id}/cobros. */
	public static class CobroRequest {
		public BigDecimal monto;
		public String formaPago;
		public String referencia;
	}

	/** Credit sales with a balance, oldest first. */
	@GetMapping("/cobros/pendientes")
	public ResponseEntity<List<CuentaPorCobrar>> pendientes() {
		return ResponseEntity.ok(service.pendientes());
	}

	@GetMapping("/ventas/{id}/cobros")
	public ResponseEntity<List<Cobro>> porVenta(@PathVariable Long id) {
		if (ventaService.getById(id) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(service.porVenta(id));
	}

	@PostMapping("/ventas/{id}/cobros")
	public ResponseEntity<Cobro> registrar(@PathVariable Long id, @RequestBody CobroRequest body) {
		Cobro c = service.registrar(id, body.monto, body.formaPago, body.referencia);
		return c == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(c, HttpStatus.CREATED);
	}

	@PostMapping("/ventas/{id}/cobros/{cobroId}/anular")
	public ResponseEntity<Cobro> anular(@PathVariable Long id, @PathVariable Long cobroId,
			@RequestBody Map<String, String> body) {
		Cobro c = service.anular(id, cobroId, body.get("motivo"));
		return c == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(c);
	}
}
