package com.marithe.sysfact.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.service.AuditoriaService;

/** Read-only access to the audit log (administrators only, see WebSecurityConfig). */
@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

	@Autowired
	private AuditoriaService service;

	/**
	 * Entries newest first. All filters are optional; desde and hasta are days (yyyy-MM-dd), both
	 * included.
	 */
	@GetMapping()
	public ResponseEntity<List<Auditoria>> buscar(
			@RequestParam(required = false) String entidad,
			@RequestParam(required = false) String accion,
			@RequestParam(required = false) Long usuarioId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer limite) {
		return ResponseEntity.ok(service.buscar(entidad, accion, usuarioId, inicio(desde),
				hasta == null ? null : inicio(hasta.plusDays(1)), limite));
	}

	private static Date inicio(LocalDate dia) {
		return dia == null ? null : Date.from(dia.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
