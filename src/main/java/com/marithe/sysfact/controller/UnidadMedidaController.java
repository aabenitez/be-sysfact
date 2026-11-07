package com.marithe.sysfact.controller;

import com.marithe.sysfact.model.UnidadMedida;
import com.marithe.sysfact.service.UnidadMedidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/unidades-de-medida")
public class UnidadMedidaController {

	@Autowired
	private UnidadMedidaService service;
	
	@GetMapping()
	public ResponseEntity<List<UnidadMedida>> listarUnidadesDeMedida(UnidadMedida obj) {
		List<UnidadMedida> list = service.getList(obj);
		return ResponseEntity.ok(list);
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<UnidadMedida> listarPorId(@PathVariable Long id) {

		UnidadMedida obj = service.getById(id);
		if (obj == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return ResponseEntity.ok(obj);
		}

	}
	
	
	@PostMapping()
	public ResponseEntity<UnidadMedida> insert(@RequestBody @Valid UnidadMedida obj) {
		UnidadMedida objInsert = service.insert(obj);
		return new ResponseEntity<>(objInsert, HttpStatus.CREATED);
	}
	
	
	@PutMapping("/{id}")
	public ResponseEntity<UnidadMedida> update(@RequestBody @Valid UnidadMedida obj, @PathVariable Long id) {
		UnidadMedida objCurrent = service.getById(id);
		if (objCurrent == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		obj.setId(id);
		service.update(obj);
		return new ResponseEntity<>(obj, HttpStatus.OK);
	}
	
	
	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		UnidadMedida obj = service.getById(id);
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
