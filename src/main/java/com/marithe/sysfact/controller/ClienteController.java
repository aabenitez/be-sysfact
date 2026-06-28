package com.marithe.sysfact.controller;

import com.marithe.sysfact.model.Cliente;
import com.marithe.sysfact.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
	
	@Autowired
	private ClienteService service;
	
	@GetMapping()
	public ResponseEntity<List<Cliente>> listarClientes(Cliente obj) {
		List<Cliente> list = service.getAll(obj);
		return ResponseEntity.ok(list);
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<Cliente> listarPorId(@PathVariable Long id) {

		Cliente obj = service.findById(id);
		if (obj == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return ResponseEntity.ok(obj);
		}

	}
	
	
	@PostMapping()
	public ResponseEntity<Cliente> insert(@RequestBody @Valid Cliente obj) {
		Cliente objInsert = service.insert(obj);
		return new ResponseEntity<>(objInsert, HttpStatus.CREATED);
	}
	
	
	@PutMapping("/{id}")
	public ResponseEntity<Cliente> update(@RequestBody @Valid Cliente obj, @PathVariable Long id) {
		Cliente objCurrent = service.findById(id);
		if (objCurrent == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		obj.setId(id);
		service.update(obj);
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}
	
	
	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		Cliente obj = service.findById(id);
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
