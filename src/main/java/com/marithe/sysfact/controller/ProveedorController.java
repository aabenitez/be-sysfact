package com.marithe.sysfact.controller;

import com.marithe.sysfact.dao.ProveedorDao;
import com.marithe.sysfact.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Solo lectura por ahora: lo usa el formulario de artículos para elegir el proveedor. */
@RestController
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorDao proveedorDao;

    @GetMapping()
    public ResponseEntity<List<Proveedor>> listar() {
        return ResponseEntity.ok(proveedorDao.getAll());
    }
}
