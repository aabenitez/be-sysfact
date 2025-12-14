package py.com.ventasjdbc.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import py.com.ventasjdbc.model.Rol;
import py.com.ventasjdbc.service.RolService;

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
