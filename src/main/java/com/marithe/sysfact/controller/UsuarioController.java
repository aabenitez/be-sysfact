package com.marithe.sysfact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Usuario;
import com.marithe.sysfact.service.UsuarioService;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestScope
@RequestMapping("/usuarios")
public class UsuarioController {

	@Autowired
	private UsuarioService userService;

	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@GetMapping()
	public ResponseEntity<List<Usuario>> getAll(Usuario obj) {
		List<Usuario> list = userService.getAll(obj);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Usuario> findById(@PathVariable Long id) {
		try {
			Usuario user = userService.findById(id);
			LOGGER.log(Level.FINE, "user found:", user);

			if (user == null) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return ResponseEntity.ok(user);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping()
	public ResponseEntity<Usuario> insert(@RequestBody @Valid Usuario obj) {
		Usuario user = userService.insert(obj);
		return new ResponseEntity<>(user, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Usuario> update(@RequestBody @Valid Usuario obj, @PathVariable Long id) {
		Usuario objCurrent = userService.findById(id);
		if (objCurrent == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		obj.setId(id);
		userService.update(obj);
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userService.delete(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
