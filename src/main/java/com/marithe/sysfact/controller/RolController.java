package com.marithe.sysfact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.marithe.sysfact.model.Rol;
import com.marithe.sysfact.service.RolService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {
	
	@Autowired
	private RolService service;
	
	@GetMapping()
	public ResponseEntity<List<Rol>> listarRoles(Rol obj) {
		List<Rol> list = service.getList(obj);
		return ResponseEntity.ok(list);
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<Rol> listarPorId(@PathVariable Long id) {

		Rol obj = service.getById(id);
		if (obj == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return ResponseEntity.ok(obj);
		}

	}
	
	
	@PostMapping()
	public ResponseEntity<Rol> insert(@RequestBody @Valid Rol obj) {
		Rol objInsert = service.insert(obj);
		return new ResponseEntity<>(objInsert, HttpStatus.CREATED);
	}
	
	
	@PutMapping("/{id}")
	public ResponseEntity<Rol> update(@RequestBody @Valid Rol obj, @PathVariable Long id) {
		Rol objCurrent = service.getById(id);
		if (objCurrent == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		obj.setId(id);
		service.update(obj);
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}
	
	
	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		Rol obj = service.getById(id);
		if (obj == null) {
			// logger.info("No se encontró Departamento con id: " + id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			service.delete(id);
			// logger.info("Se eliminó el registro: " + departamento);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

}
