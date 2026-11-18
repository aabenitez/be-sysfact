package com.marithe.sysfact.controller;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marithe.sysfact.dto.CambiarPasswordDTO;
import com.marithe.sysfact.dto.RecuperarPasswordDTO;
import com.marithe.sysfact.service.RecuperarClaveService;

/** Public endpoints (no login) for "I forgot my password". */
@RestController
@RequestMapping("/recuperar-clave")
public class RecuperarClaveController {

	@Autowired
	private RecuperarClaveService recuperarClaveService;

	/** Sends the recovery link. Always answers 200, whether or not the e-mail is registered. */
	@PostMapping("/solicitar")
	public ResponseEntity<Void> solicitar(@RequestBody @Valid RecuperarPasswordDTO body) {
		recuperarClaveService.solicitar(body.getCorreo());
		return ResponseEntity.ok().build();
	}

	/** Lets the page say up front whether the link still works: {"valido": true|false}. */
	@PostMapping("/validar")
	public ResponseEntity<Map<String, Boolean>> validar(@RequestBody Map<String, String> body) {
		boolean valido = recuperarClaveService.esValido(body.get("token"));
		return ResponseEntity.ok(Collections.singletonMap("valido", valido));
	}

	/** Sets the new password; 409 with the reason when the link is no longer valid. */
	@PostMapping("/cambiar")
	public ResponseEntity<Void> cambiar(@RequestBody @Valid CambiarPasswordDTO body) {
		recuperarClaveService.cambiar(body.getToken(), body.getPassword(), body.getPasswordConfirm());
		return ResponseEntity.noContent().build();
	}
}
