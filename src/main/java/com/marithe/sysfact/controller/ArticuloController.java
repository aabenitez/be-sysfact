package com.marithe.sysfact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.service.ArticuloService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestScope
@RequestMapping("/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService service;

    @GetMapping()
    public ResponseEntity<List<Articulo>> listarArticulos(Articulo obj) {
        List<Articulo> list = service.getList(obj);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Articulo> listarPorId(@PathVariable Long id) {
        Articulo obj = service.getById(id);
        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.ok(obj);
        }
    }

    @PostMapping()
    public ResponseEntity<Articulo> insert(@RequestBody @Valid Articulo obj) {
        Articulo objInsert = service.insert(obj);
        return new ResponseEntity<>(objInsert, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> update(@RequestBody @Valid Articulo obj, @PathVariable Long id) {
        Articulo objCurrent = service.getById(id);
        if (objCurrent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        obj.setId(id);
        service.update(obj);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Articulo obj = service.getById(id);
        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}